package com.aurum.casino.game.blackjack;

import java.util.ArrayList;
import java.util.List;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Shoe;
import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.game.session.SessionState;
import com.aurum.casino.shared.error.DomainException;

/**
 * Blackjack con reglas clásicas: 6 mazos, el crupier se planta en 17, blackjack paga 3:2,
 * el crupier revisa blackjack con As o 10 visible, doblar con dos cartas, dividir una vez
 * (Ases divididos reciben una sola carta).
 */
public class BlackjackState implements SessionState<BlackjackView> {

    public static final int DECKS = 6;
    private static final int DEALER_STANDS_ON = 17;
    private static final String HIDDEN = "??";

    private Shoe shoe;
    private List<BlackjackHand> hands = new ArrayList<>();
    private List<Card> dealer = new ArrayList<>();
    private int active;
    private boolean finished;

    private BlackjackState() {
        // deserialización
    }

    private BlackjackState(Shoe shoe, long stake) {
        this.shoe = shoe;
        this.hands.add(new BlackjackHand(stake, false));
    }

    public static BlackjackState deal(long stake, RandomSource random) {
        return deal(stake, Shoe.shuffled(DECKS, random));
    }

    /** Reparto con un sabot dado (permite tests deterministas). */
    public static BlackjackState deal(long stake, Shoe shoe) {
        BlackjackState state = new BlackjackState(shoe, stake);
        BlackjackHand hand = state.hands.get(0);
        hand.add(shoe.draw());
        state.dealer.add(shoe.draw());
        hand.add(shoe.draw());
        state.dealer.add(shoe.draw());

        if (hand.isBlackjack() || state.dealerHasBlackjack()) {
            state.resolve();
        }
        return state;
    }

    // ── Acciones del jugador

    public void apply(BlackjackAction action) {
        requireOpen();
        switch (action) {
            case HIT -> hit();
            case STAND -> stand();
            case DOUBLE -> doubleDown();
            case SPLIT -> split();
        }
    }

    private void hit() {
        currentHand().add(shoe.draw());
        advance();
    }

    private void stand() {
        currentHand().stand();
        advance();
    }

    private void doubleDown() {
        BlackjackHand hand = currentHand();
        if (!hand.canDouble()) {
            throw DomainException.invalidAction("Solo se puede doblar con las dos primeras cartas");
        }
        hand.doubleDown(shoe.draw());
        advance();
    }

    private void split() {
        BlackjackHand hand = currentHand();
        if (hands.size() > 1 || !hand.canSplit()) {
            throw DomainException.invalidAction("Solo se puede dividir un par, una vez por mano");
        }
        boolean aces = hand.holdsAces();
        BlackjackHand second = new BlackjackHand(hand.stake(), true);
        second.add(hand.removeSecond());
        BlackjackHand first = new BlackjackHand(hand.stake(), true);
        first.add(hand.cards().get(0));

        first.add(shoe.draw());
        second.add(shoe.draw());
        if (aces) {
            first.stand();
            second.stand();
        }
        hands = new ArrayList<>(List.of(first, second));
        active = 0;
        advance();
    }

    // ── Flujo interno

    /** Pasa a la siguiente mano pendiente; si no queda ninguna, juega el crupier. */
    private void advance() {
        while (active < hands.size() && hands.get(active).isDone()) {
            active++;
        }
        if (active >= hands.size()) {
            resolve();
        }
    }

    private void resolve() {
        boolean anyAlive = hands.stream().anyMatch(h -> !h.isBust());
        boolean playerNatural = hands.size() == 1 && hands.get(0).isBlackjack();
        if (anyAlive && !playerNatural && !dealerHasBlackjack()) {
            while (BlackjackHand.total(dealer) < DEALER_STANDS_ON) {
                dealer.add(shoe.draw());
            }
        }
        int dealerTotal = BlackjackHand.total(dealer);
        boolean dealerNatural = dealerHasBlackjack();
        for (BlackjackHand hand : hands) {
            hand.settle(outcomeOf(hand, dealerTotal, dealerNatural));
        }
        active = hands.size() - 1;
        finished = true;
    }

    private static HandOutcome outcomeOf(BlackjackHand hand, int dealerTotal, boolean dealerNatural) {
        if (hand.isBust()) {
            return HandOutcome.BUST;
        }
        if (hand.isBlackjack()) {
            return dealerNatural ? HandOutcome.PUSH : HandOutcome.BLACKJACK;
        }
        if (dealerNatural) {
            return HandOutcome.LOSE;
        }
        int total = hand.total();
        if (dealerTotal > BlackjackHand.BLACKJACK || total > dealerTotal) {
            return HandOutcome.WIN;
        }
        return total == dealerTotal ? HandOutcome.PUSH : HandOutcome.LOSE;
    }

    private boolean dealerHasBlackjack() {
        return dealer.size() == 2 && BlackjackHand.total(dealer) == BlackjackHand.BLACKJACK;
    }

    private BlackjackHand currentHand() {
        return hands.get(active);
    }

    private void requireOpen() {
        if (finished) {
            throw DomainException.invalidAction("La mano ya terminó");
        }
    }

    // ── SessionState

    @Override
    public long totalStake() {
        return hands.stream().mapToLong(BlackjackHand::stake).sum();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public long payout() {
        return hands.stream().mapToLong(BlackjackHand::payout).sum();
    }

    @Override
    public String summary() {
        String player = hands.stream().map(h -> String.valueOf(h.total()))
                .reduce((a, b) -> a + "/" + b).orElse("-");
        HandOutcome main = hands.get(0).outcome();
        return "Jugador " + player + " vs crupier " + BlackjackHand.total(dealer) + " · " + label(main);
    }

    private static String label(HandOutcome outcome) {
        return switch (outcome) {
            case BLACKJACK -> "Blackjack";
            case WIN -> "Gana";
            case PUSH -> "Empate";
            case LOSE -> "Pierde";
            case BUST -> "Se pasó";
            case PENDING -> "En juego";
        };
    }

    @Override
    public BlackjackView view() {
        List<BlackjackView.HandView> handViews = hands.stream()
                .map(h -> new BlackjackView.HandView(codes(h.cards()), h.total(), h.isSoft(), h.stake(),
                        h.doubled(), h.outcome(), finished ? h.payout() : 0L))
                .toList();

        List<String> dealerCards = finished
                ? codes(dealer)
                : List.of(dealer.get(0).code(), HIDDEN);
        int dealerTotal = finished
                ? BlackjackHand.total(dealer)
                : BlackjackHand.total(List.of(dealer.get(0)));

        BlackjackHand current = finished ? null : currentHand();
        return new BlackjackView(handViews, active, dealerCards, dealerTotal, finished,
                current != null,
                current != null,
                current != null && current.canDouble(),
                current != null && hands.size() == 1 && current.canSplit());
    }

    private static List<String> codes(List<Card> cards) {
        return cards.stream().map(Card::code).toList();
    }

    List<BlackjackHand> hands() {
        return hands;
    }
}
