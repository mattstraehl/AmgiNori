package com.matthias.android.amginori.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.matthias.android.amginori.R;

public abstract class ConfirmationDialogFragment extends DialogFragment {

    private int mTextId;

    public ConfirmationDialogFragment(int textId) {
        mTextId = textId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(mTextId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirm();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cancel();
                    }
                })
                .create();
    }

    public abstract void confirm();

    public void cancel() {
    }
}
