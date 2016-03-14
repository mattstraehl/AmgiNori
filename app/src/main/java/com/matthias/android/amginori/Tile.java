package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.List;

public class Tile extends Button implements Card.CardObserver {

    private Card mCard;

    private Card.CardObserver mNext;

    public static final LightingColorFilter MATERIAL_GREY_500 = new LightingColorFilter(0xffa3a3a3, 0x000000);
    public static final LightingColorFilter MATERIAL_DEEP_ORANGE_700 = new LightingColorFilter(0xffc53929, 0x000000);

    public Tile(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTransformationMethod(null);
    }

    public void init(List<Card> cards) {
        setCard(CardLibrary.get(getContext()).nextCard(cards));
    }

    public boolean match(Tile other) {
        return mCard.match(other.getCard());
    }

    @Override
    public boolean isEnabled() {
        return mCard.isEnabled();
    }

    public Card getCard() {
        return mCard;
    }

    public void setCard(Card card) {
        mCard = card.isMarked() ? card.active() : card;
        this.setText(mCard.mToDisplay);
        if (mCard.isDisabled()) {
            this.getBackground().setColorFilter(MATERIAL_DEEP_ORANGE_700);
        } else {
            this.getBackground().setColorFilter(null);
            this.setAlpha(mCard.getAlpha());
        }
        mCard.addObserver(this);
    }

    @Override
    public void onNotify(Card card) {
        mCard = card;
        resetAlpha();
        if (card.isActive()) {
            getBackground().setColorFilter(null);
            this.setAlpha(mCard.getAlpha());
        } else if (card.isMarked()) {
            getBackground().setColorFilter(Tile.MATERIAL_GREY_500);
        } else if (card.isDisabled()) {
            getBackground().setColorFilter(Tile.MATERIAL_DEEP_ORANGE_700);
        }
    }

    private void resetAlpha() {
        this.setAlpha(1f);
    }

    @Override
    public Card.CardObserver getNext() {
        return mNext;
    }

    @Override
    public void setNext(Card.CardObserver next) {
        mNext = next;
    }
}
