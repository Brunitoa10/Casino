package com.aurum.casino.game.blackjack;

import java.util.ArrayList;
import java.util.List;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Rank;

/** Una mano del jugador con su propia apuesta (puede haber dos tras dividir). */
public class BlackjackHand {

    public static final int BLACKJACK = 21;

    private List<Card> cards = new ArrayList<>();
    private long stake;
    private boolean doubled;
    private boolean stood;
    private boolean fromSplit;
    private HandOutcome outcome = HandOutcome.PENDING;

    private BlackjackHand() {
        // deserialización
    }

    BlackjackHand(long stake, boolean fromSplit) {
        this.stake = stake;
        this.fromSplit = fromSplit;
    }

    /** Total óptimo: los Ases valen 11 salvo que eso pase de 21. */
    public static int total(List<Card> cards) {
        int total = 0;
        int aces = 0;
        for (Card card : cards) {
            total += card.rank().blackjackValue();
            if (card.rank() == Rank.ACE) {
                aces++;
            }
        }
        while (total > BLACKJACK && aces-- > 0) {
            total -= 10;
        }
        return total;
    }

    public static boolean isSoft(List<Card> cards) {
        int hard = cards.stream().mapToInt(c -> c.rank() == Rank.ACE ? 1 : c.rank().blackjackValue()).sum();
        return cards.stream().anyMatch(c -> c.rank() == Rank.ACE) && hard + 10 <= BLACKJACK;
    }

    void add(Card card) {
        cards.add(card);
    }

    Card removeSecond() {
        return cards.remove(1);
    }

    void stand() {
        stood = true;
    }

    void doubleDown(Card card) {
        stake *= 2;
        doubled = true;
        cards.add(card);
        stood = true;
    }

    void settle(HandOutcome result) {
        this.outcome = result;
    }

    public int total() {
        return total(cards);
    }

    public boolean isSoft() {
        return isSoft(cards);
    }

    public boolean isBlackjack() {
        return !fromSplit && cards.size() == 2 && total() == BLACKJACK;
    }

    public boolean isBust() {
        return total() > BLACKJACK;
    }

    /** La mano ya no admite decisiones: plantada, pasada o con 21. */
    public boolean isDone() {
        return stood || isBust() || total() == BLACKJACK;
    }

    public boolean canDouble() {
        return !isDone() && cards.size() == 2;
    }

    public boolean canSplit() {
        return !isDone() && cards.size() == 2
                && cards.get(0).rank().blackjackValue() == cards.get(1).rank().blackjackValue();
    }

    public boolean holdsAces() {
        return cards.size() >= 1 && cards.get(0).rank() == Rank.ACE;
    }

    /** Monto devuelto según el resultado (incluye la apuesta cuando corresponde). */
    public long payout() {
        return switch (outcome) {
            case BLACKJACK -> stake + stake * 3 / 2;
            case WIN -> stake * 2;
            case PUSH -> stake;
            case LOSE, BUST, PENDING -> 0L;
        };
    }

    public List<Card> cards() {
        return List.copyOf(cards);
    }

    public long stake() {
        return stake;
    }

    public boolean doubled() {
        return doubled;
    }

    public HandOutcome outcome() {
        return outcome;
    }
}
