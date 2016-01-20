package com.matthias.android.amginori;

import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

public final class TileBar {

    private final HorizontalScrollView mScrollView;
    private final ViewGroup mTiles;

    private final List<Tile> mTilesUnordered = new ArrayList<>();

    public TileBar(HorizontalScrollView scrollView) {
        mScrollView = scrollView;
        mTiles = (ViewGroup) mScrollView.getChildAt(0);
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            mTilesUnordered.add((Tile) mTiles.getChildAt(i));
        }
    }

    public void initCards(List<Card> cards) {
        for (Tile tile : mTilesUnordered) {
            Card card = cards.get((int) (cards.size() * Math.random()));
            tile.setCard(card.copied());
        }
    }

    public void reset(List<Card> cards) {
        mScrollView.setScrollX(0);
        for (Tile tile : mTilesUnordered) {
            tile.initValues(cards);
            tile.getBackground().setColorFilter(null);
        }
    }

    public Tile maybeOneTileContains(int x, int y) {
        for (Tile tile : mTilesUnordered) {
            Rect rect = new Rect();
            tile.getHitRect(rect);
            rect.inset(25, 25);
            if (tile.isEnabled() && rect.contains(x + mScrollView.getScrollX(), y - mScrollView.getTop())) {
                return tile;
            }
        }
        return null;
    }

    public boolean matchAvailable(TileBar other) {
        for (Tile tile0 : mTilesUnordered) {
            for (Tile tile1 : other.mTilesUnordered) {
                if (tile0.match(tile1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Card> getCards() {
        ArrayList<Card> result = new ArrayList<>();
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            result.add(tile.getCard());
        }
        return result;
    }

    public void setCards(ArrayList<Card> cards) {
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            tile.setCard(cards.get(i));
        }
    }

    public HorizontalScrollView getScrollView() {
        return mScrollView;
    }
}
