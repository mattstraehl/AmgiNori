package com.matthias.android.amginori;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;

import java.io.File;

public class MainFragment extends Fragment {

    private static final int FILE_SELECT_CODE = 0;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 1;

    private int mLevel = 9;

    private Button mStartButton;
    private Button mImportButton;
    private Button mResumeButton;
    private EditText mFront;
    private EditText mBack;

    private Uri mUri;

    ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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
                SharedPreferencesHelper.get(getActivity()).remove("CollectionName");
                getActivity().getApplicationContext().deleteDatabase(Anki2DbHelper.DATABASE_NAME);
                CardLibrary.get(getActivity()).refresh();
                invalidateSavedGame();
                updateUI();
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
                invalidateSavedGame();
                Intent intent = BoardFragment.newIntent(getActivity(), mLevel);
                startActivity(intent);
            }
        });

        mResumeButton = (Button) view.findViewById(R.id.resume_button);
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = BoardFragment.newIntent(getActivity(), mLevel);
                startActivity(intent);
            }
        });

        mImportButton = (Button) view.findViewById(R.id.import_button);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
                } else {
                    displayFileChooser();
                }
            }
        });

        RadioButton optionEasy = (RadioButton) view.findViewById(R.id.option_easy);
        RadioButton optionHard = (RadioButton) view.findViewById(R.id.option_hard);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((RadioButton) v).isChecked();
                switch (v.getId()) {
                    case R.id.option_easy:
                        if (checked)
                            mLevel = 5;
                        break;
                    case R.id.option_hard:
                        if (checked)
                            mLevel = 3;
                        break;
                }
            }
        };
        optionEasy.setOnClickListener(listener);
        optionHard.setOnClickListener(listener);

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

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            mUri = data.getData();
            mProgress.show();
            new ImportTask().execute(mUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayFileChooser();
            }
        }
    }

    private void displayFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/apkg");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, null), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "Please install a File Manager.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(CardLibrary.get(getActivity()).size() + " cards available");
        mResumeButton.setEnabled(SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false));
    }

    private void invalidateSavedGame() {
        SharedPreferencesHelper.get(getActivity()).remove("SavedGameValid");
    }

    private class ImportTask extends AsyncTask<Uri, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... uris) {
            return AnkiPackageImporter.importAnkiPackage(getActivity(), Anki2DbHelper.DATABASE_NAME, uris[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                CardLibrary.get(getActivity()).refresh();
                persistCollectionName(mUri);
                invalidateSavedGame();
                updateUI();
                Toast.makeText(getActivity(), CardLibrary.get(getActivity()).size() + " cards imported.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Selected file is not a valid Anki Package.", Toast.LENGTH_LONG).show();
            }
            mProgress.dismiss();
        }

        private void persistCollectionName(Uri uri) {
            String collectionName = new File(uri.getPath()).getName();
            int pos = collectionName.lastIndexOf(".");
            if (pos > 0) {
                collectionName = collectionName.substring(0, pos);
            }
            SharedPreferencesHelper.get(getActivity()).putString("CollectionName", collectionName);
        }
    }
}
