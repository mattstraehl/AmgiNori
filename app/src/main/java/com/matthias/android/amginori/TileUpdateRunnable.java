package com.matthias.android.amginori;

public final class TileUpdateRunnable implements Runnable {

    private static final long DELAY = 2000l;

    private final TileBar mTileBar0;
    private final TileBar mTileBar1;
    private final GameOverCallback mCallback;

    private long mCount = 0l;

    public TileUpdateRunnable(TileBar tileBar0, TileBar tileBar1, GameOverCallback callback) {
        mTileBar0 = tileBar0;
        mTileBar1 = tileBar1;
        mCallback = callback;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            mCount++;
            if (mCount % 11 == 0) {
                addTile(mTileBar0, mTileBar1);
            }
            updateTiles(mTileBar0, mTileBar1);
            try {
                Thread.sleep(TileUpdateRunnable.DELAY);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void addTile(final TileBar tileBar0, final TileBar tileBar1) {
        tileBar0.getTiles().post(new Runnable() {
            @Override
            public void run() {
                tileBar0.addTile();
                tileBar1.addTile();
            }
        });
    }

    private void updateTiles(final TileBar tileBar0, final TileBar tileBar1) {
        tileBar0.getTiles().post(new Runnable() {
            @Override
            public void run() {
                tileBar0.updateTiles();
                tileBar1.updateTiles();
                if (TileBar.isGameOver(tileBar0, tileBar1)) {
                    mCallback.gameOverCallback();
                }
            }
        });
    }

    public interface GameOverCallback {
        void gameOverCallback();
    }
}
