package com.aurum.casino;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:file:aurum-test?mode=memory&cache=shared")
@AutoConfigureMockMvc
class CasinoApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void fullPlayerJourney() throws Exception {
        String token = register(uniqueName("tester_"));

        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10_000));

        JsonNode spin = postJson("/api/games/slots/spin", token, "{\"stake\":100}", 200);
        long expected = 10_000 - 100 + spin.get("payout").asLong();
        assertThat(spin.get("balance").asLong()).isEqualTo(expected);
        assertThat(spin.at("/outcome/reels")).hasSize(3);

        postJson("/api/games/roulette/spin", token,
                "{\"bets\":[{\"type\":\"RED\",\"amount\":50},{\"type\":\"STRAIGHT\",\"value\":7,\"amount\":10}]}", 200);
        postJson("/api/games/dice/roll", token, "{\"stake\":20,\"target\":60,\"direction\":\"UNDER\"}", 200);

        mvc.perform(get("/api/rounds").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mvc.perform(get("/api/rounds/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rounds").value(3))
                .andExpect(jsonPath("$.wagered").value(180));
    }

    @Test
    void rejectsInvalidBetsAndMissingSession() throws Exception {
        String token = register(uniqueName("rules_"));

        JsonNode tooBig = postJson("/api/games/slots/spin", token, "{\"stake\":999999}", 400);
        assertThat(tooBig.get("code").asText()).isEqualTo("INVALID_BET");

        JsonNode badBet = postJson("/api/games/roulette/spin", token,
                "{\"bets\":[{\"type\":\"STRAIGHT\",\"value\":99,\"amount\":5}]}", 400);
        assertThat(badBet.get("code").asText()).isEqualTo("INVALID_BET");

        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void singleShotTableGames() throws Exception {
        String token = register(uniqueName("tables_"));

        JsonNode baccarat = postJson("/api/games/baccarat/deal", token, "{\"player\":0,\"banker\":50,\"tie\":5}", 200);
        assertThat(baccarat.at("/outcome/playerCards").size()).isBetween(2, 3);

        JsonNode keno = postJson("/api/games/keno/play", token, "{\"stake\":10,\"picks\":[3,17,40,66]}", 200);
        assertThat(keno.at("/outcome/drawn")).hasSize(20);

        JsonNode plinko = postJson("/api/games/plinko/drop", token, "{\"stake\":10,\"risk\":\"HIGH\"}", 200);
        assertThat(plinko.at("/outcome/path")).hasSize(12);
    }

    @Test
    void statefulGamesKeepTheirSessionBetweenRequests() throws Exception {
        String token = register(uniqueName("state_"));

        // Minas: abrir, intentar abrir otra (conflicto), descubrir hasta terminar
        JsonNode mines = postJson("/api/games/mines/start", token, "{\"stake\":100,\"mines\":3}", 200);
        assertThat(mines.get("finished").asBoolean()).isFalse();
        assertThat(mines.at("/state/minePositions")).hasSize(0);
        JsonNode conflict = postJson("/api/games/mines/start", token, "{\"stake\":100,\"mines\":3}", 409);
        assertThat(conflict.get("code").asText()).isEqualTo("ACTIVE_SESSION");

        JsonNode step = mines;
        for (int tile = 0; tile < 25 && !step.get("finished").asBoolean(); tile++) {
            step = postJson("/api/games/mines/reveal", token, "{\"tile\":" + tile + "}", 200);
        }
        assertThat(step.at("/state/minePositions")).hasSize(3);

        // Blackjack: repartir y plantarse siempre termina la mano
        JsonNode hand = postJson("/api/games/blackjack/deal", token, "{\"stake\":50}", 200);
        if (!hand.get("finished").asBoolean()) {
            mvc.perform(get("/api/games/blackjack/active").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            while (!hand.get("finished").asBoolean()) {
                hand = postJson("/api/games/blackjack/action", token, "{\"action\":\"STAND\"}", 200);
            }
        }
        mvc.perform(get("/api/games/blackjack/active").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Video póker: repartir y cambiar
        postJson("/api/games/video-poker/deal", token, "{\"stake\":10}", 200);
        JsonNode poker = postJson("/api/games/video-poker/draw", token, "{\"held\":[0,1]}", 200);
        assertThat(poker.get("finished").asBoolean()).isTrue();

        // Crash: arrancar y retirarse enseguida
        postJson("/api/games/crash/start", token, "{\"stake\":10}", 200);
        JsonNode crash = postJson("/api/games/crash/cashout", token, "", 200);
        assertThat(crash.get("finished").asBoolean()).isTrue();
    }

    /** Nombre único que respeta el máximo de 20 caracteres del registro. */
    private static String uniqueName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String register(String username) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"secret123\"}";
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private JsonNode postJson(String url, String token, String body, int expectedStatus) throws Exception {
        String response = mvc.perform(post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }
}
