package com.matthias.android.amginori;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.matthias.android.amginori.persistence.Anki2DbHelper;
import com.matthias.android.amginori.persistence.AnkiPackageImporter;

public class MainFragment extends Fragment {

    private Button mStartButton;
    private Button mImportButton;
    private Button mResumeButton;
    private EditText mFront;
    private EditText mBack;

    ProgressDialog mProgress;

    private static final int FILE_SELECT_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        CardLibrary.get(getActivity()).refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_card_browser:
                Intent intent = new Intent(getActivity(), CardListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_clear_cards:
                getActivity().getApplicationContext().deleteDatabase(Anki2DbHelper.DATABASE_NAME);
                CardLibrary.get(getActivity().getApplicationContext()).refresh();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mStartButton = (Button) view.findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BoardActivity.class);
                startActivity(intent);
            }
        });

        mImportButton = (Button) view.findViewById(R.id.import_button);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/apkg");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, null), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Please install a File Manager.", Toast.LENGTH_LONG).show();
                }
            }
        });

        mResumeButton = (Button) view.findViewById(R.id.resume_button);
        mResumeButton.setEnabled(false);

        mFront = (EditText) view.findViewById(R.id.front_text);
        mFront.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFront.getText().clear();
                }
            }
        });

        mBack = (EditText) view.findViewById(R.id.back_text);
        mBack.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBack.getText().clear();
                }
            }
        });

        mStartButton.setFocusableInTouchMode(true);
        mStartButton.requestFocus();

        mProgress = new ProgressDialog(view.getContext());
        mProgress.setCancelable(false);
        mProgress.setMessage("Loading...");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        updateSubtitle();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData();
            mProgress.show();
            new ImportTask().execute(uri);
        }
    }

    private void updateSubtitle() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(CardLibrary.get(getActivity()).size() + " cards available");
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.option_easy:
                if (checked)
                    break;
            case R.id.option_hard:
                if (checked)
                    break;
        }
    }

    private class ImportTask extends AsyncTask<Uri, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... uris) {
            return AnkiPackageImporter.importAnkiPackage(getActivity(), Anki2DbHelper.DATABASE_NAME, uris[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                CardLibrary.get(getActivity().getApplicationContext()).refresh();
                updateSubtitle();
                Toast.makeText(getActivity(), CardLibrary.get(getActivity()).size() + " cards imported.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Selected file is not a valid Anki Package.", Toast.LENGTH_LONG).show();
            }
            mProgress.dismiss();
        }
    }
}
