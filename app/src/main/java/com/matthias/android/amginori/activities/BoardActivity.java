package com.matthias.android.amginori.activities;

import androidx.fragment.app.Fragment;

import com.matthias.android.amginori.fragments.BoardFragment;

public class BoardActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new BoardFragment();
    }
}
