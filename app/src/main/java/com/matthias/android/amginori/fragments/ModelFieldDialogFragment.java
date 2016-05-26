package com.matthias.android.amginori.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.matthias.android.amginori.R;

import java.util.ArrayList;

public class ModelFieldDialogFragment extends DialogFragment {

    public static final String MODEL_ID = "ModelId";
    public static final String MODEL_FIELDS = "ModelFields";
    public static final String MODEL_FIELD_INDEXES = "ModelFieldIndexes";

    private final ArrayList<Integer> mSelectedFields = new ArrayList();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.text_select_front_and_back)
                .setMultiChoiceItems(getArguments().getStringArray(MODEL_FIELDS), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mSelectedFields.add(which);
                                    if (mSelectedFields.size() == 3) {
                                        ((AlertDialog) dialog).getListView().setItemChecked(mSelectedFields.get(0), false);
                                        mSelectedFields.subList(0, 1).clear();
                                    }
                                } else if (mSelectedFields.contains(which)) {
                                    mSelectedFields.remove(Integer.valueOf(which));
                                }
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.putExtra(MODEL_ID, getArguments().getString(MODEL_ID));
                        if (mSelectedFields.size() == 0) {
                            mSelectedFields.add(0);
                        }
                        if (mSelectedFields.size() == 1) {
                            mSelectedFields.add(1);
                        }
                        intent.putIntegerArrayListExtra(MODEL_FIELD_INDEXES, mSelectedFields);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                })
                .create();
    }

    public static ModelFieldDialogFragment newInstance(String modelId, String[] modelFields) {
        ModelFieldDialogFragment result = new ModelFieldDialogFragment();
        Bundle args = new Bundle();
        args.putString(MODEL_ID, modelId);
        args.putStringArray(MODEL_FIELDS, modelFields);
        result.setArguments(args);
        return result;
    }
}
