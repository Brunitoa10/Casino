package com.aurum.casino.game.videopoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Shoe;
import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.game.session.SessionState;
import com.aurum.casino.shared.error.DomainException;

/** Video póker de una mano: reparto de 5, el jugador retiene y se cambia el resto una vez. */
public class VideoPokerState implements SessionState<VideoPokerView> {

    public static final int HAND_SIZE = 5;

    private Shoe deck;
    private List<Card> cards = new ArrayList<>();
    private List<Boolean> held = new ArrayList<>(Collections.nCopies(HAND_SIZE, false));
    private long stake;
    private boolean finished;
    private PokerHand result;

    private VideoPokerState() {
        // deserialización
    }

    public static VideoPokerState deal(long stake, RandomSource random) {
        return deal(stake, Shoe.shuffled(1, random));
    }

    public static VideoPokerState deal(long stake, Shoe deck) {
        VideoPokerState state = new VideoPokerState();
        state.deck = deck;
        state.stake = stake;
        for (int i = 0; i < HAND_SIZE; i++) {
            state.cards.add(deck.draw());
        }
        return state;
    }

    /** Reemplaza todas las cartas no retenidas y liquida la mano. */
    public void draw(Set<Integer> heldPositions) {
        if (finished) {
            throw DomainException.invalidAction("La mano ya terminó");
        }
        for (Integer position : heldPositions) {
            if (position == null || position < 0 || position >= HAND_SIZE) {
                throw DomainException.invalidAction("Posición de carta inválida");
            }
        }
        for (int i = 0; i < HAND_SIZE; i++) {
            boolean keep = heldPositions.contains(i);
            held.set(i, keep);
            if (!keep) {
                cards.set(i, deck.draw());
            }
        }
        result = PokerHandEvaluator.evaluate(cards);
        finished = true;
    }

    private PokerHand currentHand() {
        return finished ? result : PokerHandEvaluator.evaluate(cards);
    }

    @Override
    public long totalStake() {
        return stake;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public long payout() {
        return finished ? stake * result.multiplier() : 0L;
    }

    @Override
    public String summary() {
        return currentHand().label();
    }

    @Override
    public VideoPokerView view() {
        PokerHand hand = currentHand();
        return new VideoPokerView(cards.stream().map(Card::code).toList(), List.copyOf(held), hand,
                hand.label(), finished, payout());
    }
}
