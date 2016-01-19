package com.matthias.android.amginori;

import android.support.v4.app.Fragment;

public class BoardActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new BoardFragment();
    }
}
