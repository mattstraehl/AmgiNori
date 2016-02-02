package com.matthias.android.amginori;

import android.content.Context;

import com.matthias.android.amginori.persistence.Anki2DbHelper;
import com.matthias.android.amginori.persistence.Anki2DbSchema.NotesTable;

import java.util.ArrayList;
import java.util.List;

public final class CardLibrary {

    private static CardLibrary sCardLibrary;

    private List<Card> mCards = new ArrayList<>();

    private int mSize = 0;

    private Context mContext;

    private CardLibrary(Context context) {
        mContext = context.getApplicationContext();
    }

    public static CardLibrary get(Context context) {
        if (sCardLibrary == null) {
            sCardLibrary = new CardLibrary(context);
        }
        return sCardLibrary;
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

    public int size() {
        return mSize;
    }
}
