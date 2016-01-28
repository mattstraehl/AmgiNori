package com.matthias.android.amginori.persistence;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AnkiPackageImporter {

    private static final String ZIP_FILE_ENTRY = "collection.anki2";

    private static final int FILE_COPY_BUFFER_SIZE = 2048;

    public static boolean importAnkiPackage(Context context, String dbName, Uri file) {
        Anki2DbHelper database = new Anki2DbHelper(context);
        database.getReadableDatabase();
        ZipFile zipFile = null;
        OutputStream out = null;
        try {
            zipFile = new ZipFile(new File(file.getPath()), ZipFile.OPEN_READ);
            ZipEntry ze = zipFile.getEntry(ZIP_FILE_ENTRY);
            if (ze == null || ze.isDirectory()) {
                return false;
            }
            InputStream in = zipFile.getInputStream(ze);
            out = new FileOutputStream(context.getDatabasePath(dbName).getAbsolutePath());
            byte[] buffer = new byte[FILE_COPY_BUFFER_SIZE];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            out.flush();
            out.close();
            zipFile.close();
        } catch (IOException e) {
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }
        database.close();
        return true;
    }
}
