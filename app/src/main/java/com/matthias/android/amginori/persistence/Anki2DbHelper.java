package com.matthias.android.amginori.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;

import com.matthias.android.amginori.Card;
import com.matthias.android.amginori.CardLibrary;
import com.matthias.android.amginori.persistence.Anki2DbSchema.AmgiNoriCardsTable;
import com.matthias.android.amginori.persistence.Anki2DbSchema.ColTable;
import com.matthias.android.amginori.persistence.Anki2DbSchema.NotesTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Anki2DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CardCollection";
    private static final int VERSION = 1;

    private static final String IMAGE_PATH_REGEX = "<img.*src=[\"'](.*)[\"'].*/?>";
    private static final String SOUND_PATH_REGEX = "\\[sound:(.*)\\]";

    private final String DATABASE_PATH;
    private final Context mContext;

    public Anki2DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        mContext = context;
    }

    private boolean databaseExists() {
        return new File(DATABASE_PATH).exists();
    }

    private boolean tableExists(String table) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE name='"
                + table + "' and type='table'", null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        database.close();
        return result > 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AmgiNoriCardsTable.NAME + "("
                        + AmgiNoriCardsTable.Cols.ID + " integer primary key autoincrement, "
                        + AmgiNoriCardsTable.Cols.FRONT + " text not null, "
                        + AmgiNoriCardsTable.Cols.BACK + " text not null)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addCard(String front, String back) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AmgiNoriCardsTable.Cols.FRONT, front);
        values.put(AmgiNoriCardsTable.Cols.BACK, back);
        database.insert(AmgiNoriCardsTable.NAME, null, values);
        database.close();
    }

    public void deleteCard(Card card) {
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = AmgiNoriCardsTable.Cols.ID + " = ?";
        String[] selectionArgs = {String.valueOf(card.mId)};
        database.delete(AmgiNoriCardsTable.NAME, selection, selectionArgs);
        database.close();
    }

    public List<Card> getAllCards() {
        List<Card> result = new LinkedList<>();
        if (!databaseExists() || !tableExists(AmgiNoriCardsTable.NAME)) {
            return result;
        }

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + AmgiNoriCardsTable.Cols.ID
                + ", " + AmgiNoriCardsTable.Cols.FRONT
                + ", " + AmgiNoriCardsTable.Cols.BACK
                + " FROM " + AmgiNoriCardsTable.NAME
                + " ORDER BY " + AmgiNoriCardsTable.Cols.ID + " DESC", null);

        Long id;
        String front, back;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(0);
                front = cursor.getString(1);
                back = cursor.getString(2);
                result.add(new Card(id, front, back));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return result;
    }

    public boolean requiredTablesExist() {
        return databaseExists() && tableExists(NotesTable.NAME) && tableExists(ColTable.NAME);
    }

    public Map<String, String[]> getCollectionModels() {
        SQLiteDatabase database = this.getReadableDatabase();

        // Get all model ids
        List<String> modelIds = new LinkedList<>();
        Cursor cursor = database.rawQuery("SELECT DISTINCT " + NotesTable.Cols.MID + " FROM " + NotesTable.NAME, null);
        if (cursor.moveToFirst()) {
            do {
                modelIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Get lists of model fields
        Map<String, String[]> result = new HashMap<>();
        cursor = database.rawQuery("SELECT " + ColTable.Cols.MODELS + " FROM " + ColTable.NAME, null);
        if (cursor.moveToFirst()) {
            try {
                JSONObject collection = new JSONObject(cursor.getString(0));
                for (String modelId : modelIds) {
                    JSONArray flds = collection.getJSONObject(modelId).getJSONArray("flds");
                    if (flds.length() > 2) {
                        String[] modelFields = new String[flds.length()];
                        for (int i = 0; i < flds.length(); i++) {
                            modelFields[i] = flds.getJSONObject(i).getString("name");
                        }
                        result.put(modelId, modelFields);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        database.close();
        return result;
    }

    public int copyCardsOfAnkiCollection(Map<String, Integer[]> modelFieldIndexes) {
        SQLiteDatabase database = this.getWritableDatabase();

        // Copy existing cards
        if (CardLibrary.get(mContext).size() > 0) {
            List<Card> cards = CardLibrary.get(mContext).getAllCards();
            for (Card card : cards) {
                insertCard(database, card.mFront, card.mBack);
            }
        }

        // Copy newly imported collection
        Cursor cursor = database.rawQuery("SELECT " + NotesTable.Cols.FLDS
                + ", " + NotesTable.Cols.MID + " FROM " + NotesTable.NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                String card = cursor.getString(0);
                String[] fields = card.split(NotesTable.FIELD_SEPARATOR, -1);
                Integer[] indexes = modelFieldIndexes.get(cursor.getString(1));
                if (indexes == null) {
                    indexes = new Integer[]{0, 1};
                }
                // Strip all formatting and media path information
                String front = fields[indexes[0]].replaceAll(IMAGE_PATH_REGEX, "").replaceAll(SOUND_PATH_REGEX, "");
                String back = fields[indexes[1]].replaceAll(IMAGE_PATH_REGEX, "").replaceAll(SOUND_PATH_REGEX, "");
                front = Html.fromHtml(front).toString();
                back = Html.fromHtml(back).toString();
                if (!front.isEmpty() && !back.isEmpty()) {
                    insertCard(database, front, back);
                    count++;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return count;
    }

    private static void insertCard(SQLiteDatabase database, String front, String back) {
        ContentValues values = new ContentValues();
        values.put(AmgiNoriCardsTable.Cols.FRONT, front);
        values.put(AmgiNoriCardsTable.Cols.BACK, back);
        database.insert(AmgiNoriCardsTable.NAME, null, values);
    }
}
