package com.matthias.android.amginori.persistence;

import android.content.Context;
import android.net.Uri;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class AnkiPackageImporter {

    private static final String ZIP_FILE_ENTRY = "collection.anki2";

    private static final int FILE_COPY_BUFFER_SIZE = 2048;

    public static int importAnkiPackage(Context context, String dbName, Uri file) {
        Anki2DbHelper database = new Anki2DbHelper(context);
        database.getReadableDatabase();
        ZipInputStream in = null;
        OutputStream out = null;
        try {
            in = new ZipInputStream(context.getContentResolver().openInputStream(file));
            ZipEntry ze = null;
            for (ZipEntry e; (e = in.getNextEntry()) != null;) {
                if (ZIP_FILE_ENTRY.equals(e.getName())) {
                    ze = e;
                    break;
                }
            }
            if (ze == null || ze.isDirectory()) {
                return -1;
            }
            out = new FileOutputStream(context.getDatabasePath(dbName).getAbsolutePath());
            byte[] buffer = new byte[FILE_COPY_BUFFER_SIZE];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } catch (IOException e) {
            return -1;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            database.close();
        }
        return database.copyCardsOfAnkiCollection();
    }
}
