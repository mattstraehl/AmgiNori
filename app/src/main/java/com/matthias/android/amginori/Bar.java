package com.matthias.android.amginori;

import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

public final class Bar {

    private final List<Tile> mTiles = new ArrayList<>();

    private final HorizontalScrollView mScrollView;

    public Bar(HorizontalScrollView scrollView) {
        mScrollView = scrollView;
    }

    public void reset(List<Card> cards) {
        mScrollView.setScrollX(0);
        for (Tile tile : mTiles) {
            tile.initValues(cards);
            tile.getBackground().setColorFilter(null);
        }
    }

    public ArrayList<Card> getCards() {
        ArrayList<Card> result = new ArrayList<>();
        /*for (Tile tile : mTiles) {
            result.add(tile.getCard());
        }*/
        ViewGroup viewGroup = (ViewGroup) mScrollView.getChildAt(0);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            Tile tile = (Tile) viewGroup.getChildAt(i);
            result.add(tile.getCard());
        }
        return result;
    }

    public void initCards(List<Card> cards) {
        for (Tile tile : mTiles) {
            Card card = cards.get((int) (cards.size() * Math.random()));
            tile.setCard(card.copied());
        }
    }

    public void setCards(ArrayList<Card> cards) {
        int index = 0;
        for (Tile tile : mTiles) {
            tile.setCard(cards.get(index));
            index++;
        }
    }

    public List<Tile> getTiles() {
        return mTiles;
    }

    public HorizontalScrollView getScrollView() {
        return mScrollView;
    }
}
