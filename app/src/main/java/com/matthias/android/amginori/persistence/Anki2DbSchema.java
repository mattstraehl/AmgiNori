package com.matthias.android.amginori.persistence;

public final class Anki2DbSchema {

    public static final class ColTable {

        public static final String NAME = "col";

        public static final class Cols {
            public static final String MODELS = "models";
        }
    }

    public static final class NotesTable {

        public static final String NAME = "notes";

        public static final String FIELD_SEPARATOR = "\u001f";

        public static final class Cols {
            public static final String MID = "mid";
            public static final String FLDS = "flds";
        }
    }
}
