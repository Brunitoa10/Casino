package com.aurum.casino.game.baccarat;

import java.util.ArrayList;
import java.util.List;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Shoe;

/**
 * Una mano de Punto Banco con las reglas fijas de tercera carta.
 * No hay decisiones del jugador: todo lo determina el reparto.
 */
public final class BaccaratCoup {

    private static final int NATURAL = 8;
    private static final int PLAYER_STANDS_ON = 6;

    private final List<Card> player = new ArrayList<>();
    private final List<Card> banker = new ArrayList<>();

    private BaccaratCoup() {
    }

    public static BaccaratCoup play(Shoe shoe) {
        BaccaratCoup coup = new BaccaratCoup();
        coup.player.add(shoe.draw());
        coup.banker.add(shoe.draw());
        coup.player.add(shoe.draw());
        coup.banker.add(shoe.draw());

        if (coup.playerTotal() >= NATURAL || coup.bankerTotal() >= NATURAL) {
            return coup;
        }
        Card playerThird = null;
        if (coup.playerTotal() < PLAYER_STANDS_ON) {
            playerThird = shoe.draw();
            coup.player.add(playerThird);
        }
        if (bankerDraws(coup.bankerTotal(), playerThird)) {
            coup.banker.add(shoe.draw());
        }
        return coup;
    }

    /** Tabla clásica de la banca según su total y la tercera carta del jugador. */
    static boolean bankerDraws(int bankerTotal, Card playerThird) {
        if (playerThird == null) {
            return bankerTotal <= 5;
        }
        int third = playerThird.rank().baccaratValue();
        return switch (bankerTotal) {
            case 0, 1, 2 -> true;
            case 3 -> third != 8;
            case 4 -> third >= 2 && third <= 7;
            case 5 -> third >= 4 && third <= 7;
            case 6 -> third == 6 || third == 7;
            default -> false;
        };
    }

    public static int total(List<Card> cards) {
        return cards.stream().mapToInt(c -> c.rank().baccaratValue()).sum() % 10;
    }

    public int playerTotal() {
        return total(player);
    }

    public int bankerTotal() {
        return total(banker);
    }

    public BaccaratSide winner() {
        int p = playerTotal();
        int b = bankerTotal();
        if (p == b) {
            return BaccaratSide.TIE;
        }
        return p > b ? BaccaratSide.PLAYER : BaccaratSide.BANKER;
    }

    public List<Card> playerCards() {
        return List.copyOf(player);
    }

    public List<Card> bankerCards() {
        return List.copyOf(banker);
    }
}
