package com.matthias.android.amginori.activities;

import android.support.v4.app.Fragment;

import com.matthias.android.amginori.fragments.CardListFragment;

public class CardListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CardListFragment();
    }
}
