package com.matthias.android.amginori;

import android.content.Context;

import com.matthias.android.amginori.persistence.Anki2DbHelper;
import com.matthias.android.amginori.persistence.Anki2DbSchema.NotesTable;
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;
import com.matthias.android.amginori.utils.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class CardLibrary {

    private static final int CARD_POOL_SIZE = 12;

    private static CardLibrary sCardLibrary;

    private List<Card> mCards = new ArrayList<>();
    private LinkedList<Card> mCardPool = new LinkedList<>();

    private int mSize = 0;

    private Context mContext;

    private CardLibrary(Context context) {
        mContext = context.getApplicationContext();
    }

    public static CardLibrary get(Context context) {
        if (sCardLibrary == null) {
            sCardLibrary = new CardLibrary(context);
            sCardLibrary.refresh();
        }
        return sCardLibrary;
    }

    public Card nextCard(List<Card> cards) {
        if (mCardPool.isEmpty()) {
            Card[] newCards = new Card[CARD_POOL_SIZE];
            // Insert cards in this order: [c_1, r_n, c_2, r_n-1, ... c_n-1, r_2, c_n, r_1]
            for (int i = 0; i < CARD_POOL_SIZE / 2; i++) {
                newCards[i * 2] = cards.get((int) (cards.size() * Math.random())).copy();
            }
            for (int i = 0; i < CARD_POOL_SIZE / 2; i++) {
                newCards[CARD_POOL_SIZE - 1 - (i * 2)] = newCards[i * 2].reversedCopy();
            }
            Collections.addAll(mCardPool, newCards);
        }
        return mCardPool.poll();
    }

    public ArrayList<Card> getRandomCards(int size) {
        ArrayList<Card> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = mCards.get((int) (mCards.size() * Math.random()));
            result.add(card);
            result.add(card.reversedCopy());
        }
        return result;
    }

    public void refresh() {
        Anki2DbHelper database = new Anki2DbHelper(mContext);
        mCards = new ArrayList<>();
        List<String> cards = database.getAllCards();
        if (cards.isEmpty()) {
            mCards.add(new Card("NO CARDS", "AVAILABLE"));
            mSize = 0;
        } else {
            for (String card : cards) {
                String[] fields = card.split(NotesTable.FIELD_SEPARATOR, -1);
                mCards.add(new Card(fields[0].trim(), fields[1].trim()));
            }
            mSize = mCards.size();
        }
    }

    public void persist() {
        SharedPreferencesHelper.get(mContext).putString("CardPool", Base64.encodeObject(mCardPool));
    }

    public void restore() {
        String cardPool = SharedPreferencesHelper.get(mContext).getString("CardPool", "");
        mCardPool = Base64.decodeString(cardPool);
    }

    public void reset() {
        mCardPool.clear();
    }

    public int size() {
        return mSize;
    }
}
