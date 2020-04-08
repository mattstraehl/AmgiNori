package com.matthias.android.amginori;

import android.content.Context;

import com.matthias.android.amginori.persistence.AmgiNoriDbHelper;
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;
import com.matthias.android.amginori.utils.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class CardLibrary {

    private static CardLibrary sCardLibrary;

    private List<Card> mCards = new ArrayList<>();
    private LinkedList<Card> mCardPool = new LinkedList<>();

    private int mSize = 0;
    private int mCardPoolSize = Level.EASY.cardPoolSize;

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
            Card[] newCards = new Card[mCardPoolSize];
            // Insert cards in this order: [c_1, r_n, c_2, r_n-1, ... c_n-1, r_2, c_n, r_1]
            int offset = (int) (cards.size() * Math.random());
            for (int i = 0; i < mCardPoolSize / 2; i++) {
                newCards[i * 2] = cards.get(offset++ % cards.size()).copy();
            }
            for (int i = 0; i < mCardPoolSize / 2; i++) {
                newCards[mCardPoolSize - 1 - (i * 2)] = newCards[i * 2].reversedCopy();
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
        Collections.shuffle(result);
        return result;
    }

    public List<Card> getAllCards() {
        return mCards;
    }

    public void addCard(String front, String back) {
        AmgiNoriDbHelper database = new AmgiNoriDbHelper(mContext);
        database.addCard(front, back);
        refresh();
    }

    public void deleteCard(Card card) {
        AmgiNoriDbHelper database = new AmgiNoriDbHelper(mContext);
        database.deleteCard(card);
        refresh();
    }

    public void refresh() {
        AmgiNoriDbHelper database = new AmgiNoriDbHelper(mContext);
        mCards = database.getAllCards();
        if (mCards.isEmpty()) {
            mCards.add(new Card(mContext.getString(R.string.no_cards).toUpperCase(),
                    mContext.getString(R.string.available).toUpperCase()));
            mSize = 0;
        } else {
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

    public void setCardPoolSize(int cardPoolSize) {
        mCardPoolSize = cardPoolSize;
    }
}
