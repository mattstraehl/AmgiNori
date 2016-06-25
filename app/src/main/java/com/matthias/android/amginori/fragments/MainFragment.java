package com.matthias.android.amginori.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.matthias.android.amginori.CardLibrary;
import com.matthias.android.amginori.Level;
import com.matthias.android.amginori.R;
import com.matthias.android.amginori.activities.CardListActivity;
import com.matthias.android.amginori.persistence.Anki2DbHelper;
import com.matthias.android.amginori.persistence.AnkiPackageImporter;
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    private static final int SELECT_FILE_CODE = 0;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 1;
    private static final int CLEAR_CARDS_CONFIRMATION_DIALOG_CODE = 2;
    private static final int SELECT_MODEL_FIELDS_CODE = 3;

    private Level mLevel = Level.EASY;

    private Button mResumeButton;
    private EditText mFront;
    private EditText mBack;
    private TextView mHelpText;

    private Uri mUri;

    private ProgressDialog mProgressDialog;
    private boolean mIsImportTaskRunning = false;

    private Map<String, String[]> mCollectionModels;
    private final Map<String, Integer[]> mModelFieldIndexes = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
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
        FragmentManager manager = getFragmentManager();
        DialogFragment dialog;
        switch (item.getItemId()) {
            case R.id.menu_item_card_browser:
                Intent intent = new Intent(getActivity(), CardListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_clear_cards:
                dialog = ConfirmationDialogFragment.newInstance(R.string.text_confirm_clear_cards);
                dialog.setTargetFragment(this, CLEAR_CARDS_CONFIRMATION_DIALOG_CODE);
                dialog.show(manager, "dialog");
                return true;
            case R.id.menu_item_about:
                dialog = new AboutDialogFragment();
                dialog.show(manager, "dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button startButton = (Button) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
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

        final RadioButton optionEasy = (RadioButton) view.findViewById(R.id.option_easy);
        final RadioButton optionHard = (RadioButton) view.findViewById(R.id.option_hard);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((RadioButton) v).isChecked();
                switch (v.getId()) {
                    case R.id.option_easy:
                        if (checked)
                            mLevel = Level.EASY;
                        break;
                    case R.id.option_hard:
                        if (checked)
                            mLevel = Level.HARD;
                        break;
                }
            }
        };
        optionEasy.setOnClickListener(listener);
        optionHard.setOnClickListener(listener);
        view.post(new Runnable() {
            @Override
            public void run() {
                mLevel = optionHard.isChecked() ? Level.HARD : Level.EASY;
            }
        });

        Button addButton = (Button) view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrorSet = true;
                if (mFront.getText().toString().trim().isEmpty()) {
                    mFront.setError(getString(R.string.cannot_be_blank));
                    noErrorSet = false;
                }
                if (mBack.getText().toString().trim().isEmpty()) {
                    mBack.setError(getString(R.string.cannot_be_blank));
                    noErrorSet = false;
                }
                if (noErrorSet) {
                    CardLibrary.get(getActivity()).addCard(mFront.getText().toString().trim(), mBack.getText().toString().trim());
                    invalidateSavedGame();
                    updateUI();
                    mFront.requestFocus();
                    mFront.getText().clear();
                    mBack.getText().clear();
                    Toast.makeText(getActivity(), R.string.text_card_added, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button importButton = (Button) view.findViewById(R.id.import_button);
        importButton.setOnClickListener(new View.OnClickListener() {
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

        mFront = (EditText) view.findViewById(R.id.front_text);
        mBack = (EditText) view.findViewById(R.id.back_text);
        mHelpText = (TextView) view.findViewById(R.id.help_text);

        mProgressDialog = new ProgressDialog(view.getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.text_loading));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (mIsImportTaskRunning) {
            mProgressDialog.show();
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == SELECT_FILE_CODE) {
            mUri = data.getData();
            mIsImportTaskRunning = true;
            mProgressDialog.show();
            new ImportCollectionTask().execute(mUri);
        } else if (requestCode == CLEAR_CARDS_CONFIRMATION_DIALOG_CODE) {
            SharedPreferencesHelper.get(getActivity()).remove("CollectionName");
            getActivity().getApplicationContext().deleteDatabase(Anki2DbHelper.DATABASE_NAME);
            CardLibrary.get(getActivity()).refresh();
            invalidateSavedGame();
            updateUI();
        } else if (requestCode == SELECT_MODEL_FIELDS_CODE) {
            Object[] indexes = data.getIntegerArrayListExtra(ModelFieldDialogFragment.MODEL_FIELD_INDEXES).toArray();
            mModelFieldIndexes.put(data.getStringExtra(ModelFieldDialogFragment.MODEL_ID),
                    Arrays.copyOf(indexes, indexes.length, Integer[].class));
            if (mCollectionModels.size() == mModelFieldIndexes.size()) {
                new CopyCollectionTask().execute();
            } else {
                createModelFieldDialog();
            }
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

    @Override
    public void onDetach() {
        super.onDetach();
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void displayFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, null), SELECT_FILE_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.text_install_file_manager, Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        int librarySize = CardLibrary.get(getActivity()).size();
        String cardsAvailable = getResources().getQuantityString(R.plurals.numberOfCardsAvailable, librarySize, librarySize);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(cardsAvailable);
        mResumeButton.setEnabled(SharedPreferencesHelper.get(getActivity()).getBoolean("SavedGameValid", false));
        mHelpText.setVisibility(librarySize == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void invalidateSavedGame() {
        SharedPreferencesHelper.get(getActivity()).remove("SavedGameValid");
    }

    private void createModelFieldDialog() {
        Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) mCollectionModels.entrySet().toArray()[mModelFieldIndexes.size()];
        FragmentManager manager = getFragmentManager();
        ModelFieldDialogFragment dialog = ModelFieldDialogFragment.newInstance(entry.getKey(), entry.getValue());
        dialog.setTargetFragment(this, SELECT_MODEL_FIELDS_CODE);
        dialog.setCancelable(false);
        dialog.show(manager, "dialog");
    }

    private class ImportCollectionTask extends AsyncTask<Uri, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... params) {
            return AnkiPackageImporter.importAnkiPackage(getActivity(), Anki2DbHelper.DATABASE_NAME, params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result || !new Anki2DbHelper(getActivity()).requiredTablesExist()) {
                mProgressDialog.dismiss();
                mIsImportTaskRunning = false;
                Toast.makeText(getActivity(), R.string.text_invalid_anki_package, Toast.LENGTH_LONG).show();
            } else {
                mCollectionModels = new Anki2DbHelper(getActivity()).getCollectionModels();
                if (mCollectionModels.isEmpty()) {
                    new CopyCollectionTask().execute();
                } else {
                    createModelFieldDialog();
                }
            }
        }
    }

    private class CopyCollectionTask extends AsyncTask<Void, Void, Integer> {

        private final Anki2DbHelper mDatabase = new Anki2DbHelper(getActivity());

        @Override
        protected Integer doInBackground(Void... params) {
            return mDatabase.copyCardsOfAnkiCollection(mModelFieldIndexes);
        }

        @Override
        protected void onPostExecute(Integer result) {
            CardLibrary.get(getActivity()).refresh();
            persistCollectionName(mUri);
            invalidateSavedGame();
            updateUI();
            String cardsImported = getResources().getQuantityString(R.plurals.numberOfCardsImported, result, result);
            Toast.makeText(getActivity(), cardsImported, Toast.LENGTH_LONG).show();
            mModelFieldIndexes.clear();
            mProgressDialog.dismiss();
            mIsImportTaskRunning = false;
        }

        private void persistCollectionName(Uri uri) {
            String collectionName;
            if ("content".equals(uri.getScheme())) {
                Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                collectionName = cursor.getString(nameIndex);
                cursor.close();
            } else {
                collectionName = new File(uri.getPath()).getName();
            }
            int pos = collectionName.lastIndexOf(".");
            if (pos > 0) {
                collectionName = collectionName.substring(0, pos);
            }
            SharedPreferencesHelper.get(getActivity()).putString("CollectionName", collectionName);
        }
    }
}
