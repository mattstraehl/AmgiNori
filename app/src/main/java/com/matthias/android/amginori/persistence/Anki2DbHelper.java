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
import com.matthias.android.amginori.persistence.Anki2DbSchema.NotesTable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public final class Anki2DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CardCollection";
    private static final int VERSION = 1;

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

    public int copyCardsOfAnkiCollection() {
        if (!databaseExists() || !tableExists(NotesTable.NAME)) {
            return -1;
        }

        SQLiteDatabase database = this.getWritableDatabase();

        // Copy existing cards
        if (CardLibrary.get(mContext).size() > 0) {
            List<Card> cards = CardLibrary.get(mContext).getAllCards();
            for (Card card : cards) {
                insertCard(database, card.mFront, card.mBack);
            }
        }

        // Copy newly imported cards
        Cursor cursor = database.rawQuery("SELECT " + NotesTable.Cols.FLDS
                + " FROM " + NotesTable.NAME, null);

        int count = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                String card = cursor.getString(0);
                String[] fields = card.split(NotesTable.FIELD_SEPARATOR, -1);
                insertCard(database, Html.fromHtml(fields[0]).toString(), Html.fromHtml(fields[1]).toString());
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
