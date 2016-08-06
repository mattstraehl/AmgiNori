package com.matthias.android.amginori;

public enum Level {

    EASY(R.string.option_easy, 50, 6, 5, 4000l),
    HARD(R.string.option_hard, 50, 10, 3, 3200l);

    public final int textId;
    public final int initialCardCount;
    public final int cardPoolSize;
    public final int matchCountPerNewCard;
    public final long millisPerUpdate;

    Level(int textId, int initialCardCount, int cardPoolSize, int matchCountPerNewCard, long millisPerUpdate) {
        this.textId = textId;
        this.initialCardCount = initialCardCount;
        this.cardPoolSize = cardPoolSize;
        this.matchCountPerNewCard = matchCountPerNewCard;
        this.millisPerUpdate = millisPerUpdate;
    }
}
