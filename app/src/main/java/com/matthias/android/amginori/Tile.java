package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.List;

public class Tile extends Button implements Card.CardSubscriber {

    private Card mCard;

    private Card.CardSubscriber mNext;

    private static final LightingColorFilter MATERIAL_GREY_900 = new LightingColorFilter(0xff212121, 0x000000);
    private static final LightingColorFilter MATERIAL_GREY_500 = new LightingColorFilter(0xffa3a3a3, 0x000000);
    private static final LightingColorFilter MATERIAL_DEEP_ORANGE_700 = new LightingColorFilter(0xffc53929, 0x000000);
    private static final LightingColorFilter AMGI_NORI_GREEN = new LightingColorFilter(0xff6cbf00, 0x000000);

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

    public Card getCard() {
        return mCard;
    }

    public void setCard(Card card) {
        mCard = card.isMarked() ? card.active() : card;
        this.setText(mCard.mToDisplay);
        if (mCard.isDisabled()) {
            this.getBackground().setColorFilter(MATERIAL_DEEP_ORANGE_700);
        } else {
            this.getBackground().setColorFilter(MATERIAL_GREY_900);
            this.setAlpha(mCard.getAlpha());
        }
        mCard.addSubscriber(this);
    }

    @Override
    public void onNotify(Card card) {
        mCard = card;
        resetAlpha();
        if (card.isActive()) {
            this.getBackground().setColorFilter(MATERIAL_GREY_900);
            this.setAlpha(mCard.getAlpha());
        } else if (card.isMarked()) {
            this.getBackground().setColorFilter(MATERIAL_GREY_500);
        } else if (card.isMatched()) {
            this.getBackground().setColorFilter(AMGI_NORI_GREEN);
        } else if (card.isDisabled()) {
            this.getBackground().setColorFilter(MATERIAL_DEEP_ORANGE_700);
        }
    }

    private void resetAlpha() {
        this.setAlpha(1f);
    }

    @Override
    public Card.CardSubscriber getNext() {
        return mNext;
    }

    @Override
    public void setNext(Card.CardSubscriber next) {
        mNext = next;
    }
}
