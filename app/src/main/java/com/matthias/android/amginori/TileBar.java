package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

public final class TileBar {

    private final Context mContext;
    private final HorizontalScrollView mScrollView;
    private final ViewGroup mTiles;
    private final View.OnClickListener mTileClickListener;

    private List<Card> mCards;

    public TileBar(Context context, HorizontalScrollView scrollView, View.OnClickListener tileClickListener) {
        mContext = context.getApplicationContext();
        mScrollView = scrollView;
        mTiles = (ViewGroup) mScrollView.getChildAt(0);
        mTileClickListener = tileClickListener;
    }

    public void init(List<Card> cards) {
        mCards = cards;
        mTiles.removeAllViews();
        for (int i = 0; i < 3; i++) {
            Tile tile = initTile();
            tile.init(cards);
            mTiles.addView(tile);
        }
        mScrollView.setScrollX(0);
    }

    public Tile maybeOneTileContains(int x, int y) {
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            Rect rect = new Rect();
            tile.getHitRect(rect);
            rect.inset(25, 25);
            if (tile.isEnabled() && rect.contains(x + mScrollView.getScrollX() - mScrollView.getLeft(),
                    y - mScrollView.getTop())) {
                return tile;
            }
        }
        return null;
    }

    public ArrayList<Card> getCards() {
        ArrayList<Card> result = new ArrayList<>();
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            result.add(tile.getCard());
        }
        return result;
    }

    public void setCards(ArrayList<Card> restored, List<Card> cards) {
        mCards = cards;
        mTiles.removeAllViews();
        for (int i = 0; i < restored.size(); i++) {
            Tile tile = initTile();
            tile.setCard(restored.get(i));
            mTiles.addView(tile);
        }
    }

    public void updateTiles() {
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            final Tile tile = (Tile) mTiles.getChildAt(i);
            if (tile.getCard().isEnabled()) {
                float alpha = tile.getCard().getAlpha() - 0.1f;
                if (alpha < 0 && tile.getCard().isActive()) {
                    mTiles.removeView(tile);
                } else {
                    tile.getCard().setAlpha(alpha);
                }
            }
        }
    }

    public void addTile() {
        Tile tile = initTile();
        tile.init(mCards);
        mTiles.addView(tile, (int) (Math.random() * mTiles.getChildCount()));
    }

    private Tile initTile() {
        Tile tile = (Tile) LayoutInflater.from(mContext).inflate(R.layout.tile, mTiles, false);
        tile.setOnClickListener(mTileClickListener);
        return tile;
    }

    private int enabledTileCount() {
        int result = 0;
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            if (tile.isEnabled()) {
                result++;
            }
        }
        return result;
    }

    public static boolean isGameOver(TileBar tileBar0, TileBar tileBar1) {
        return tileBar0.enabledTileCount() == 0 && tileBar1.enabledTileCount() == 0;
    }

    public HorizontalScrollView getScrollView() {
        return mScrollView;
    }

    public ViewGroup getTiles() {
        return mTiles;
    }
}
