package com.matthias.android.amginori;

import android.support.v4.app.Fragment;

public class CardListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CardListFragment();
    }
}
