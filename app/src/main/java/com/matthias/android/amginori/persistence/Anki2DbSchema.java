package com.matthias.android.amginori.persistence;

public final class Anki2DbSchema {

    public static final class NotesTable {

        public static final String NAME = "notes";

        public static final String FIELD_SEPARATOR = "\\x1f";

        public static final class Cols {
            public static final String FLDS = "flds";
        }
    }
}
