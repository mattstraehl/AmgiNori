package com.matthias.android.amginori.activities;

import androidx.fragment.app.Fragment;

import com.matthias.android.amginori.fragments.MainFragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
