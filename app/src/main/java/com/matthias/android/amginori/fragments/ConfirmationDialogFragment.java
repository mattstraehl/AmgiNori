package com.matthias.android.amginori.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.matthias.android.amginori.R;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String TEXT_ID = "textId";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(getArguments().getInt(TEXT_ID)))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
    }

    public static ConfirmationDialogFragment newInstance(int textId) {
        ConfirmationDialogFragment result = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TEXT_ID, textId);
        result.setArguments(args);
        return result;
    }
}
