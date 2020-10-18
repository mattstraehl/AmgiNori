package com.matthias.android.amginori.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.matthias.android.amginori.Card;
import com.matthias.android.amginori.CardLibrary;
import com.matthias.android.amginori.CustomLayout;
import com.matthias.android.amginori.Level;
import com.matthias.android.amginori.R;
import com.matthias.android.amginori.Tile;
import com.matthias.android.amginori.TileBar;
import com.matthias.android.amginori.TileUpdateRunnable;
import com.matthias.android.amginori.activities.BoardActivity;
import com.matthias.android.amginori.audio.AudioPlayer;
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;
import com.matthias.android.amginori.utils.Base64;
import com.matthias.android.amginori.utils.Interpolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BoardFragment extends Fragment implements TileUpdateRunnable.GameOverCallback {

    private static final String EXTRA_LEVEL = "com.matthias.android.amginori.level";
    private static final int GAME_OVER_DIALOG_CODE = 0;

    private int mMatchCount = 0;
    private int mScore;
    private int mBestScore;
    private double mScoreIncrement;
    private Level mLevel;

    private Tile mSelected0;
    private Tile mSelected1;
    private ArrayList<Card> mCardSelection;
    private ArrayList<TileBar> mTileBars = new ArrayList<>(3);
    private NavigableMap<Integer, TileBar> mBars = new TreeMap<>();

    private CustomLayout mLayout;
    private TextView mScoreView;
    private TextView mBestScoreView;

    private AudioPlayer mAudioPlayer;
    private Vibrator mVibrator;

    private Thread mUpdateThread;

    private Point mPreviousIndex;
    private int mStepSize;

    @Override
    public void onPause() {
        super.onPause();

        mUpdateThread.interrupt();
        try {
            mUpdateThread.join();
        } catch (InterruptedException e) {
        }

        SharedPreferencesHelper.get(getActivity()).putInt("LatestScore" + mLevel, mScore);
        SharedPreferencesHelper.get(getActivity()).putInt("BestScore" + mLevel, mBestScore);
        SharedPreferencesHelper.get(getActivity()).putInt("MatchCount", mMatchCount);
        SharedPreferencesHelper.get(getActivity()).putInt("Level", mLevel.ordinal());

        SharedPreferencesHelper.get(getActivity()).putString("CardSelection", Base64.encodeObject(mCardSelection));
        SharedPreferencesHelper.get(getActivity()).putString("Cards", Base64.encodeObject(TileBar.getCards(mTileBars)));

        SharedPreferencesHelper.get(getActivity()).putBoolean("SavedGameValid", true);

        CardLibrary.get(getActivity()).persist();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mAudioPlayer = new AudioPlayer(getActivity());
        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        int latestScore = 0;
        if (SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false)) {
            mMatchCount = SharedPreferencesHelper.get(getActivity()).getInt("MatchCount", 0);
            mLevel = Level.values()[SharedPreferencesHelper.get(getActivity()).getInt("Level", 0)];
        } else {
            mLevel = Level.values()[getActivity().getIntent().getIntExtra(EXTRA_LEVEL, 0)];
            latestScore = SharedPreferencesHelper.get(getActivity()).getInt("LatestScore" + mLevel, 0);
        }
        mBestScore = Math.max(latestScore, SharedPreferencesHelper.get(getActivity()).getInt("BestScore" + mLevel, 0));
        mScoreIncrement = Math.log(1 + CardLibrary.get(getActivity()).size());
        mScore = (int) (mMatchCount * mScoreIncrement);
        CardLibrary.get(getActivity()).setCardPoolSize(mLevel.cardPoolSize);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_board, container, false);
        view.post(new Runnable() {
            @Override
            public void run() {
                measureScrollViews();
            }
        });

        View.OnClickListener tileClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tile tile = (Tile) v;
                if (tile.getCard().isDisabled() || tile.getCard().isMatched()) {
                    return;
                }
                if (mSelected0 == null) {
                    mSelected0 = tile;
                    mSelected0.getCard().marked();
                } else if (mSelected0 == tile) {
                    deselectTile();
                } else {
                    mSelected1 = tile;
                    updateTiles();
                }
                view.invalidate();
            }
        };

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        Point point = new Point((int) event.getX(), (int) event.getY());
                        mLayout.mPoints.add(point);
                        List<Point> indexes = Interpolation.interpolate(mPreviousIndex, point, mStepSize);
                        mPreviousIndex = point;
                        for (Point index : indexes) {
                            if (outOfBounds(index)) {
                                continue;
                            }
                            Map.Entry<Integer, TileBar> entry = mBars.floorEntry(index.y);
                            if (entry == null) {
                                measureScrollViews();
                                entry = mBars.floorEntry(index.y);
                            }
                            TileBar tileBar = entry.getValue();
                            Tile tile = tileBar.maybeOneTileContains(index.x, index.y);
                            if (tile == null) {
                                continue;
                            }
                            if (mSelected0 == null) {
                                mSelected0 = tile;
                                mSelected0.getCard().marked();
                            } else if (mSelected1 == null) {
                                mSelected1 = tile;
                                mSelected1.getCard().marked();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (mSelected0 != null && mSelected1 != null) {
                            updateTiles();
                        } else if (mSelected0 != null) {
                            deselectTile();
                        }
                        mPreviousIndex = null;
                        mLayout.mPoints.clear();
                        break;
                }
                view.invalidate();
                return true;
            }
        });

        mLayout = (CustomLayout) view.findViewById(R.id.relative_layout);
        mStepSize = getResources().getDimensionPixelSize(R.dimen.step_size);
        mScoreView = (TextView) view.findViewById(R.id.current_score);
        mBestScoreView = (TextView) view.findViewById(R.id.best_score);

        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View child = mLayout.getChildAt(i);
            if (child instanceof HorizontalScrollView) {
                TileBar tileBar = new TileBar(getActivity(), (HorizontalScrollView) child, tileClickListener);
                mTileBars.add(tileBar);
                mLayout.mViews.add(child);
            }
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        String level = getString(mLevel.textId);
        String collectionName = SharedPreferencesHelper.get(getActivity()).getString("CollectionName", null);
        if (collectionName != null) {
            activity.getSupportActionBar().setSubtitle(level + " - " + collectionName);
        } else {
            activity.getSupportActionBar().setSubtitle(level);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false)) {
            String cardSelection = SharedPreferencesHelper.get(getActivity()).getString("CardSelection", "");
            String cards = SharedPreferencesHelper.get(getActivity()).getString("Cards", "");
            mCardSelection = Base64.decodeString(cardSelection);
            CardLibrary.get(getActivity()).restore();
            TileBar.setCards(mTileBars, Base64.<ArrayList<Card>>decodeString(cards), mCardSelection);
        } else {
            init();
        }
        updateUI();
        startUpdateThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioPlayer.release();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_board, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_restart:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void measureScrollViews() {
        mBars.clear();
        for (TileBar tileBar : mTileBars) {
            mBars.put(tileBar.getScrollView().getTop(), tileBar);
        }
    }

    private boolean outOfBounds(Point index) {
        TileBar first = mTileBars.get(0);
        TileBar last = mTileBars.get(mTileBars.size() - 1);
        return index.y < first.getScrollView().getTop() || index.y > (last.getScrollView().getTop()) + last.getScrollView().getHeight();
    }

    private void updateTiles() {
        if (mSelected0.match(mSelected1)) {
            notifyMatch();
        } else {
            notifyClash();
        }
        if (TileBar.isGameOver(mTileBars)) {
            gameOverCallback();
        }
        mSelected0 = mSelected1 = null;
    }

    private void deselectTile() {
        if (mSelected0.getCard().getAlpha() <= 0) {
            ((ViewGroup) mSelected0.getParent()).removeView(mSelected0);
        } else {
            mSelected0.getCard().active();
        }
        mSelected0 = null;
    }

    private void notifyMatch() {
        mAudioPlayer.playMatchSound();
        updateScore();
        reinsertTile(mSelected0);
        reinsertTile(mSelected1);
    }

    private void notifyClash() {
        mAudioPlayer.playClashSound();
        if (mVibrator.hasVibrator()) {
            mVibrator.vibrate(100);
        }
        mSelected0.getCard().disabled();
        mSelected1.getCard().disabled();
    }

    private void updateScore() {
        mMatchCount++;
        mScore = (int) (mMatchCount * mScoreIncrement);
        if (mMatchCount % mLevel.matchCountPerNewCard == 0) {
            mCardSelection.addAll(CardLibrary.get(getActivity()).getRandomCards(1));
        }
        mScoreView.setText(Integer.toString(mScore));
    }

    private void reinsertTile(final Tile tile) {
        tile.getCard().matched();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup viewGroup = (ViewGroup) tile.getParent();
                viewGroup.removeView(tile);
                TileBar.getShortest(mTileBars).addTile();
            }
        }, 170);
    }

    private void createGameOverDialog() {
        FragmentManager manager = getFragmentManager();
        GameOverDialogFragment dialog = new GameOverDialogFragment();
        dialog.setTargetFragment(this, GAME_OVER_DIALOG_CODE);
        dialog.setCancelable(false);
        dialog.show(manager, "dialog");
    }

    private void init() {
        mCardSelection = CardLibrary.get(getActivity()).getRandomCards(mLevel.initialCardCount);
        CardLibrary.get(getActivity()).reset();
        for (TileBar tileBar : mTileBars) {
            tileBar.init(mCardSelection);
        }
    }

    private void reset() {
        mUpdateThread.interrupt();
        mSelected0 = mSelected1 = null;
        if (mScore > mBestScore) {
            mBestScore = mScore;
        }
        mScore = 0;
        mMatchCount = 0;
        init();
        updateUI();
        startUpdateThread();
    }

    private void updateUI() {
        mScoreView.setText(Integer.toString(mScore));
        mBestScoreView.setText(Integer.toString(mBestScore));
    }

    private void startUpdateThread() {
        mUpdateThread = new Thread(new TileUpdateRunnable(mTileBars, mLevel.millisPerUpdate, this));
        mUpdateThread.start();
    }

    @Override
    public void gameOverCallback() {
        mUpdateThread.interrupt();
        createGameOverDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == GAME_OVER_DIALOG_CODE) {
            reset();
        }
    }

    public static Intent newIntent(Context packageContext, Level level) {
        Intent intent = new Intent(packageContext, BoardActivity.class);
        intent.putExtra(EXTRA_LEVEL, level.ordinal());
        return intent;
    }
}
