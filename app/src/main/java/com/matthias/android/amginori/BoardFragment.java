package com.matthias.android.amginori;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.matthias.android.amginori.persistence.SharedPreferencesHelper;
import com.matthias.android.amginori.utils.Base64;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BoardFragment extends Fragment {

    private static final String EXTRA_LEVEL = "com.matthias.android.amginori.level";

    private int mMatchCount = 0;
    private int mScore;
    private int mBestScore;
    private double mScoreIncrement;
    private int mLevel;

    private ArrayList<Card> mCards;

    private float mFirstY;

    private Tile mSelected0;
    private Tile mSelected1;

    private TileBar mTileBar0;
    private TileBar mTileBar1;
    private TileBar mTileBar2;

    private NavigableMap<Integer, TileBar> mBars = new TreeMap<>();

    private CustomLayout mLayout;
    private TextView mScoreView;
    private TextView mBestScoreView;

    private Vibrator mVibrator;

    @Override
    public void onPause() {
        super.onPause();
        if (mScore > mBestScore) {
            mBestScore = mScore;
        }

        SharedPreferencesHelper.get(getActivity()).putInt("BestScore", mBestScore);
        SharedPreferencesHelper.get(getActivity()).putInt("MatchCount", mMatchCount);
        SharedPreferencesHelper.get(getActivity()).putInt("Level", mLevel);

        SharedPreferencesHelper.get(getActivity()).putString("Cards", Base64.encodeObject(mCards));
        SharedPreferencesHelper.get(getActivity()).putString("TileBar0", Base64.encodeObject(mTileBar0.getCards()));
        SharedPreferencesHelper.get(getActivity()).putString("TileBar1", Base64.encodeObject(mTileBar1.getCards()));
        SharedPreferencesHelper.get(getActivity()).putString("TileBar2", Base64.encodeObject(mTileBar2.getCards()));

        SharedPreferencesHelper.get(getActivity()).putBoolean("SavedGameValid", true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBestScore = SharedPreferencesHelper.get(getActivity()).getInt("BestScore", 0);
        if (SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false)) {
            mMatchCount = SharedPreferencesHelper.get(getActivity()).getInt("MatchCount", 0);
        }
        mScoreIncrement = Math.log(1 + CardLibrary.get(getActivity()).size());
        mScore = (int) (mMatchCount * mScoreIncrement);
        if (getActivity().getIntent().hasExtra(EXTRA_LEVEL)) {
            mLevel = getActivity().getIntent().getIntExtra(EXTRA_LEVEL, 9);
            getActivity().getIntent().removeExtra(EXTRA_LEVEL);
        } else {
            mLevel = SharedPreferencesHelper.get(getActivity()).getInt("Level", 9);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        view.post(new Runnable() {
            @Override
            public void run() {
                mBars.put(mTileBar0.getScrollView().getTop(), mTileBar0);
                mBars.put(mTileBar1.getScrollView().getTop(), mTileBar1);
                mBars.put(mTileBar2.getScrollView().getTop(), mTileBar2);
                mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
        });

        mTileBar0 = new TileBar((HorizontalScrollView) view.findViewById(R.id.scroll_view_0));
        mTileBar1 = new TileBar((HorizontalScrollView) view.findViewById(R.id.scroll_view_1));
        mTileBar2 = new TileBar((HorizontalScrollView) view.findViewById(R.id.scroll_view_2));

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mLayout.mPoints.add(new Point((int) event.getX(), (int) event.getY()));
                        view.invalidate();
                        TileBar tileBar;
                        if (outOfBounds(event)) {
                            return true;
                        } else {
                            tileBar = mBars.floorEntry((int) event.getY()).getValue();
                        }
                        Tile tile = tileBar.maybeOneTileContains((int) event.getX(), (int) event.getY());
                        if (tile != null) {
                            if (tile == mSelected0 || tile == mSelected1) {
                                return true;
                            }
                            if (mSelected0 == null) {
                                mSelected0 = tile;
                                mSelected0.getCard().marked();
                                mFirstY = event.getY();
                            } else if (mSelected1 == null) {
                                if (!mBars.floorEntry((int) mFirstY).getKey().equals(mBars.floorEntry((int) event.getY()).getKey())) {
                                    mSelected1 = tile;
                                    mSelected1.getCard().marked();
                                }
                            }
                            break;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (mSelected0 != null && mSelected1 != null) {
                            if (mSelected0.match(mSelected1)) {
                                notifyMatch();
                            } else {
                                notifyClash();
                            }
                            if (!matchAvailable()) {
                                createDialog();
                            }
                        } else if (mSelected0 != null) {
                            mSelected0.getCard().active();
                        }
                        mSelected0 = mSelected1 = null;
                        mLayout.mPoints.clear();
                        view.invalidate();
                        break;
                }
                return true;
            }
        });

        mLayout = (CustomLayout) view.findViewById(R.id.relative_layout);
        mScoreView = (TextView) view.findViewById(R.id.current_score);
        mScoreView.setText(Integer.toString(mScore));
        mBestScoreView = (TextView) view.findViewById(R.id.best_score);
        mBestScoreView.setText(Integer.toString(mBestScore));

        String collectionName = SharedPreferencesHelper.get(getActivity()).getString("CollectionName", null);
        if (collectionName != null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setSubtitle(collectionName);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false)) {
            String cards = SharedPreferencesHelper.get(getActivity()).getString("Cards", "");
            String tileBar0 = SharedPreferencesHelper.get(getActivity()).getString("TileBar0", "");
            String tileBar1 = SharedPreferencesHelper.get(getActivity()).getString("TileBar1", "");
            String tileBar2 = SharedPreferencesHelper.get(getActivity()).getString("TileBar2", "");
            mCards = Base64.decodeString(cards);
            mTileBar0.setCards(Base64.<ArrayList<Card>>decodeString(tileBar0));
            mTileBar1.setCards(Base64.<ArrayList<Card>>decodeString(tileBar1));
            mTileBar2.setCards(Base64.<ArrayList<Card>>decodeString(tileBar2));
        } else {
            mCards = CardLibrary.get(getActivity()).getRandomCards(1);
            mTileBar0.initCards(mCards);
            mTileBar1.initCards(mCards);
            mTileBar2.initCards(mCards);
        }
    }

    private boolean outOfBounds(MotionEvent event) {
        return event.getY() < mTileBar0.getScrollView().getTop() || event.getY() > (mTileBar2.getScrollView().getTop()) + mTileBar2.getScrollView().getHeight();
    }

    private void notifyMatch() {
        updateScore();
        reinsertTiles();
    }

    private void notifyClash() {
        if (mVibrator.hasVibrator()) {
            mVibrator.vibrate(100);
        }
        mSelected0.getCard().disabled();
        mSelected1.getCard().disabled();
    }

    private void updateScore() {
        mMatchCount++;
        mScore = (int) (mMatchCount * mScoreIncrement);
        if (mMatchCount % mLevel == 0) {
            mCards.addAll(CardLibrary.get(getActivity()).getRandomCards(1));
        }
        mScoreView.setText(Integer.toString(mScore));
    }

    private void reinsertTiles() {
        ViewGroup viewGroup0 = (ViewGroup) mSelected0.getParent();
        ViewGroup viewGroup1 = (ViewGroup) mSelected1.getParent();
        viewGroup0.removeView(mSelected0);
        viewGroup1.removeView(mSelected1);

        mSelected0.initValues(mCards);
        mSelected1.initValues(mCards);

        viewGroup0.addView(mSelected0, (int) (Math.random() * viewGroup0.getChildCount()));
        viewGroup1.addView(mSelected1, (int) (Math.random() * viewGroup1.getChildCount()));
    }

    private boolean matchAvailable() {
        if (mTileBar0.matchAvailable(mTileBar1)) {
            return true;
        }
        if (mTileBar1.matchAvailable(mTileBar2)) {
            return true;
        }
        return mTileBar0.matchAvailable(mTileBar2);
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Game Over!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                reset();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void reset() {
        SharedPreferencesHelper.get(getActivity()).remove("TileBar0");
        mSelected0 = mSelected1 = null;
        if (mScore > mBestScore) {
            mBestScore = mScore;
        }
        mScore = 0;
        mMatchCount = 0;
        mCards = CardLibrary.get(getActivity()).getRandomCards(1);
        mScoreView.setText(Integer.toString(mScore));
        mBestScoreView.setText(Integer.toString(mBestScore));
        mTileBar0.reset(mCards);
        mTileBar1.reset(mCards);
        mTileBar2.reset(mCards);
    }

    public static Intent newIntent(Context packageContext, int level) {
        Intent i = new Intent(packageContext, BoardActivity.class);
        i.putExtra(EXTRA_LEVEL, level);
        return i;
    }
}
