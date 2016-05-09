package com.matthias.android.amginori;

import java.util.List;

public final class TileUpdateRunnable implements Runnable {

    private final List<TileBar> mTileBars;
    private final long mMillisPerUpdate;
    private final GameOverCallback mCallback;

    private long mCount = 0l;

    public TileUpdateRunnable(List<TileBar> tileBars, long millisPerUpdate, GameOverCallback callback) {
        mTileBars = tileBars;
        mMillisPerUpdate = millisPerUpdate;
        mCallback = callback;
    }

    @Override
    public void run() {
        long start;
        while (!Thread.currentThread().isInterrupted()) {
            start = System.currentTimeMillis();
            mCount++;
            if (mCount % 11 == 0) {
                addTile(mTileBars);
            }
            updateTiles(mTileBars);
            try {
                Thread.sleep(start + mMillisPerUpdate - System.currentTimeMillis());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void addTile(final List<TileBar> tileBars) {
        tileBars.get(0).getTiles().post(new Runnable() {
            @Override
            public void run() {
                TileBar.getShortest(mTileBars).addTile();
                TileBar.getShortest(mTileBars).addTile();
            }
        });
    }

    private void updateTiles(final List<TileBar> tileBars) {
        tileBars.get(0).getTiles().post(new Runnable() {
            @Override
            public void run() {
                for (TileBar tileBar : tileBars) {
                    tileBar.updateTiles();
                }
                if (TileBar.isGameOver(tileBars)) {
                    mCallback.gameOverCallback();
                }
            }
        });
    }

    public interface GameOverCallback {
        void gameOverCallback();
    }
}
