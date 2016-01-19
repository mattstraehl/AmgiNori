package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.List;

public class Tile extends Button implements Card.CardObserver {

    private Card mCard;

    public static final LightingColorFilter MATERIAL_GREY_500 = new LightingColorFilter(0xffa3a3a3, 0x000000);
    public static final LightingColorFilter MATERIAL_DEEP_ORANGE_700 = new LightingColorFilter(0xffc53929, 0x000000);

    public Tile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initValues(List<Card> cards) {
        Card card = cards.get((int) (cards.size() * Math.random()));
        setCard(card.copied());
    }

    public boolean match(Tile other) {
        return mCard.match(other.getCard());
    }

    @Override
    public boolean isEnabled() {
        return !mCard.isDisabled();
    }

    public Card getCard() {
        return mCard;
    }

    public void setCard(Card card) {
        mCard = card;
        this.setText(mCard.mToDisplay);
        if (mCard.isDisabled()) {
            this.getBackground().setColorFilter(MATERIAL_DEEP_ORANGE_700);
        } else {
            this.getBackground().setColorFilter(null);
        }
        mCard.addObserver(this);
    }

    @Override
    public void onNotify(Card card) {
        mCard = card;
        if (card.isActive()) {
            getBackground().setColorFilter(null);
        } else if (card.isMarked()) {
            getBackground().setColorFilter(Tile.MATERIAL_GREY_500);
        } else if (card.isDisabled()) {
            getBackground().setColorFilter(Tile.MATERIAL_DEEP_ORANGE_700);
        }
    }
}
