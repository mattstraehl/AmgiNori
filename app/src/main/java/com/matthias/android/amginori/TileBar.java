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
    private final int mInitialCount;

    private List<Card> mCards;
    private int mInset;

    public TileBar(Context context, HorizontalScrollView scrollView, View.OnClickListener tileClickListener) {
        mContext = context.getApplicationContext();
        mScrollView = scrollView;
        mTiles = (ViewGroup) mScrollView.getChildAt(0);
        mTileClickListener = tileClickListener;
        mInset = context.getResources().getDimensionPixelSize(R.dimen.inset);
        mInitialCount = mTiles.getChildCount();
    }

    public void init(List<Card> cards) {
        mCards = cards;
        mTiles.removeAllViews();
        for (int i = 0; i < mInitialCount; i++) {
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
            rect.inset(mInset, mInset);
            if (tile.getCard().isActive() && rect.contains(x + mScrollView.getScrollX() - mScrollView.getLeft(),
                    y - mScrollView.getTop())) {
                return tile;
            }
        }
        return null;
    }

    public void updateTiles() {
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            if (tile.getCard().isActive() || tile.getCard().isMarked()) {
                float alpha = tile.getCard().getAlpha() - 0.1f;
                if (alpha <= 0 && tile.getCard().isActive()) {
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

    public Tile initTile() {
        Tile tile = (Tile) LayoutInflater.from(mContext).inflate(R.layout.tile, mTiles, false);
        tile.setOnClickListener(mTileClickListener);
        return tile;
    }

    private int enabledTileCount() {
        int result = 0;
        for (int i = 0; i < mTiles.getChildCount(); i++) {
            Tile tile = (Tile) mTiles.getChildAt(i);
            if (tile.getCard().isEnabled()) {
                result++;
            }
        }
        return result;
    }

    public static ArrayList<Card> getCards(List<TileBar> tileBars) {
        ArrayList<Card> result = new ArrayList<>();
        for (TileBar tileBar : tileBars) {
            for (int i = 0; i < tileBar.mTiles.getChildCount(); i++) {
                Tile tile = (Tile) tileBar.mTiles.getChildAt(i);
                result.add(tile.getCard());
            }
        }
        return result;
    }

    public static void setCards(List<TileBar> tileBars, List<Card> restored, List<Card> cards) {
        int partitionSize = restored.size() / tileBars.size() + Math.min(restored.size() % tileBars.size(), 1);
        int i = 0;
        for (TileBar tileBar : tileBars) {
            tileBar.mCards = cards;
            tileBar.mTiles.removeAllViews();
            for (Card card : restored.subList(Math.min(i, restored.size()), Math.min(i += partitionSize, restored.size()))) {
                Tile tile = tileBar.initTile();
                tile.setCard(card);
                tileBar.mTiles.addView(tile);
            }
        }
    }

    public static TileBar getShortest(List<TileBar> tileBars) {
        TileBar result = tileBars.get((int) (tileBars.size() * Math.random()));
        for (TileBar tileBar : tileBars) {
            if (tileBar.mTiles.getChildCount() < result.mTiles.getChildCount()) {
                result = tileBar;
            }
        }
        return result;
    }

    public static boolean isGameOver(List<TileBar> tileBars) {
        int enabledTileCount = 0;
        for (TileBar tileBar : tileBars) {
            enabledTileCount += tileBar.enabledTileCount();
        }
        return enabledTileCount == 0;
    }

    public HorizontalScrollView getScrollView() {
        return mScrollView;
    }

    public ViewGroup getTiles() {
        return mTiles;
    }
}
