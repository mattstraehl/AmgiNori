package com.matthias.android.amginori;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BoardFragment extends Fragment {

    private int mLevel = 1;
    private int mScore = 0;
    private int mBestScore;

    private ArrayList<Card> mCards;

    private float mFirstY;

    private Tile mSelected0;
    private Tile mSelected1;

    private Bar mBar0;
    private Bar mBar1;
    private Bar mBar2;

    private NavigableMap<Integer, Bar> mBars = new TreeMap<>();

    private CustomLayout mLayout;
    private TextView mScoreView;

    private Vibrator mVibrator;

    private static final String PREFS_NAME = "AmgiNoriPrefsFile";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mScore = savedInstanceState.getInt("mScore", 0);
            mBestScore = savedInstanceState.getInt("mBestScore", 0);
            mCards = savedInstanceState.getParcelableArrayList("mCards");
        } else {
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            mBestScore = settings.getInt("bestScore", 0);
            mCards = CardLibrary.getRandomCards(mLevel);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mScore", mScore);
        outState.putInt("mBestScore", mBestScore);
        outState.putParcelableArrayList("mCards", mCards);
        outState.putParcelableArrayList("mBar0", mBar0.getCards());
        outState.putParcelableArrayList("mBar1", mBar1.getCards());
        outState.putParcelableArrayList("mBar2", mBar2.getCards());
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mScore > mBestScore) {
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("bestScore", mScore);
            editor.commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_board, menu);
        menu.findItem(R.id.menu_item_mode_3).setVisible(false);
        menu.findItem(R.id.menu_item_rotate).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentTransaction transaction;
        switch (item.getItemId()) {
            case R.id.menu_item_mode_1:
                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardFragment());
                transaction.commit();
                return true;
            case R.id.menu_item_mode_2:
                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardFragment());
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        view.post(new Runnable() {
            @Override
            public void run() {
                mBars.put(mBar0.getScrollView().getTop(), mBar0);
                mBars.put(mBar1.getScrollView().getTop(), mBar1);
                mBars.put(mBar2.getScrollView().getTop(), mBar2);
                mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
        });

        mBar0 = new Bar((HorizontalScrollView) view.findViewById(R.id.scroll_view_0));
        mBar1 = new Bar((HorizontalScrollView) view.findViewById(R.id.scroll_view_1));
        mBar2 = new Bar((HorizontalScrollView) view.findViewById(R.id.scroll_view_2));

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mLayout.mPoints.add(new Point((int) event.getX(), (int) event.getY()));
                        getView().invalidate();
                        Bar bar;
                        if (outOfBounds(event)) {
                            return true;
                        } else {
                            bar = mBars.floorEntry((int) event.getY()).getValue();
                        }
                        for (Tile tile : bar.getTiles()) {
                            if (contains(tile, bar.getScrollView(), event)) {
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
                        getView().invalidate();
                        break;
                }
                return true;
            }
        });

        ViewGroup tiles = (ViewGroup) view.findViewById(R.id.row_0);
        for (int i = 0; i < tiles.getChildCount(); i++) {
            mBar0.getTiles().add((Tile) tiles.getChildAt(i));
        }
        tiles = (ViewGroup) view.findViewById(R.id.row_1);
        for (int i = 0; i < tiles.getChildCount(); i++) {
            mBar1.getTiles().add((Tile) tiles.getChildAt(i));
        }
        tiles = (ViewGroup) view.findViewById(R.id.row_2);
        for (int i = 0; i < tiles.getChildCount(); i++) {
            mBar2.getTiles().add((Tile) tiles.getChildAt(i));
        }

        if (savedInstanceState == null) {
            mBar0.initCards(mCards);
            mBar1.initCards(mCards);
            mBar2.initCards(mCards);
        } else {
            mBar0.setCards(savedInstanceState.<Card>getParcelableArrayList("mBar0"));
            mBar1.setCards(savedInstanceState.<Card>getParcelableArrayList("mBar1"));
            mBar2.setCards(savedInstanceState.<Card>getParcelableArrayList("mBar2"));
        }

        mLayout = (CustomLayout) view.findViewById(R.id.relative_layout);
        mScoreView = (TextView) view.findViewById(R.id.current_score);
        mScoreView.setText(Integer.toString(mScore));
        TextView bestScoreView = (TextView) view.findViewById(R.id.best_score);
        bestScoreView.setText(Integer.toString(mBestScore));

        return view;
    }

    private boolean outOfBounds(MotionEvent event) {
        return event.getY() < mBar0.getScrollView().getTop() || event.getY() > (mBar2.getScrollView().getTop()) + mBar2.getScrollView().getHeight();
    }

    private boolean contains(Tile tile, HorizontalScrollView sv, MotionEvent event) {
        Rect rect = new Rect();
        tile.getHitRect(rect);
        rect.inset(25, 25);
        return tile.isEnabled() && rect.contains((int) event.getX() + sv.getScrollX(), (int) (event.getY() - sv.getTop()));
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
        mScore += mLevel;
        if (mScore % 9 == 0) {
            mCards.addAll(CardLibrary.getRandomCards(2));
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
        if (matchAvailable(mBar0.getTiles(), mBar1.getTiles())) {
            return true;
        }
        if (matchAvailable(mBar1.getTiles(), mBar2.getTiles())) {
            return true;
        }
        if (matchAvailable(mBar0.getTiles(), mBar2.getTiles())) {
            return true;
        }
        return false;
    }

    private boolean matchAvailable(List<Tile> list0, List<Tile> list1) {
        for (Tile tile0 : list0) {
            for (Tile tile1 : list1) {
                if (tile0.match(tile1)) {
                    return true;
                }
            }
        }
        return false;
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
        mSelected0 = mSelected1 = null;
        if (mScore > mBestScore) {
            mBestScore = mScore;
        }
        mLevel = 1;
        mScore = 0;
        mCards = CardLibrary.getRandomCards(mLevel);
        mScoreView.setText(Integer.toString(mScore));
        mBar0.reset(mCards);
        mBar1.reset(mCards);
        mBar2.reset(mCards);
    }
}
