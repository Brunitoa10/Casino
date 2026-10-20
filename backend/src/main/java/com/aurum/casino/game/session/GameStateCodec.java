package com.aurum.casino.game.session;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serializa el estado interno de los juegos por campos (no por getters):
 * así las clases de dominio no necesitan exponer setters ni datos secretos.
 */
@Component
public class GameStateCodec {

    private final ObjectMapper mapper;

    public GameStateCodec(ObjectMapper baseMapper) {
        this.mapper = baseMapper.copy()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String write(Object state) {
        try {
            return mapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el estado del juego", e);
        }
    }

    public <S> S read(String json, Class<S> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Estado de juego corrupto", e);
        }
    }
}
