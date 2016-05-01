package com.matthias.android.amginori;

public enum Level {

    EASY(R.string.option_easy, 2, 3, 4000l),
    HARD(R.string.option_hard, 10, 5, 3500l);

    public final int textId;
    public final int initialCardCount;
    public final int matchCountPerNewCard;
    public final long millisPerUpdate;

    Level(int textId, int initialCardCount, int matchCountPerNewCard, long millisPerUpdate) {
        this.textId = textId;
        this.initialCardCount = initialCardCount;
        this.matchCountPerNewCard = matchCountPerNewCard;
        this.millisPerUpdate = millisPerUpdate;
    }
}
