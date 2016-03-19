package com.matthias.android.amginori.persistence;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.matthias.android.amginori.persistence.Anki2DbSchema.NotesTable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class Anki2DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CardCollection";

    private final String DATABASE_PATH;

    private static final int VERSION = 1;

    public Anki2DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<String> getAllCards() {
        List<String> result = new LinkedList<>();
        if (!databaseExists() || !tableExists(NotesTable.NAME)) {
            return result;
        }

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + NotesTable.Cols.FLDS
                + " FROM " + NotesTable.NAME
                + " ORDER BY " + NotesTable.Cols.ID + " DESC", null);

        String card;
        if (cursor.moveToFirst()) {
            do {
                card = cursor.getString(0);
                result.add(card);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return result;
    }
}
