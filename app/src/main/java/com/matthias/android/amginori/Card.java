package com.matthias.android.amginori;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public final class Card implements Parcelable, Serializable {

    private enum CardState {
        ACTIVE, MARKED, DISABLED
    }

    public final String mFront;
    public final String mBack;
    public final boolean mShowFront;
    public final String mToDisplay;

    private final CardState mCardState;

    private volatile float mAlpha = 1f;

    private transient CardObserver mHead;

    public Card(String front, String back) {
        this(front, back, Math.random() < 0.5 ? true : false, CardState.ACTIVE, 1f);
    }

    private Card(String front, String back, boolean showFront, CardState cardState, float alpha) {
        mFront = front;
        mBack = back;
        mShowFront = showFront;
        mToDisplay = mShowFront ? mFront : mBack;
        mCardState = cardState;
        mAlpha = alpha;
    }

    public boolean match(Card other) {
        return isEnabled() && other.isEnabled() && this.equals(other);
    }

    public Card active() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.ACTIVE, mAlpha);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card marked() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.MARKED, mAlpha);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card disabled() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.DISABLED, mAlpha);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card copy() {
        return new Card(mFront, mBack, mShowFront, mCardState, mAlpha);
    }

    public Card reversedCopy() {
        return new Card(mFront, mBack, !mShowFront, mCardState, mAlpha);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (mShowFront == card.mShowFront) return false;
        return !(mBack != null ? !mBack.equals(card.mBack) : card.mBack != null);
    }

    public boolean isActive() {
        return mCardState == CardState.ACTIVE;
    }

    public boolean isEnabled() {
        return mCardState != CardState.DISABLED;
    }

    public boolean isDisabled() {
        return mCardState == CardState.DISABLED;
    }

    public boolean isMarked() {
        return mCardState == CardState.MARKED;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
        notifyObservers(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mFront);
        out.writeString(mBack);
        out.writeByte((byte) (mShowFront ? 1 : 0));
        out.writeSerializable(mCardState);
        out.writeFloat(mAlpha);
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    private Card(Parcel in) {
        mFront = in.readString();
        mBack = in.readString();
        mShowFront = in.readByte() != 0;
        mToDisplay = mShowFront ? mFront : mBack;
        mCardState = (CardState) in.readSerializable();
        mAlpha = in.readFloat();
    }

    private void notifyObservers(Card card) {
        CardObserver observer = mHead;
        while (observer != null) {
            observer.onNotify(card);
            observer = observer.getNext();
        }
    }

    public void addObserver(CardObserver observer) {
        observer.setNext(mHead);
        mHead = observer;
    }

    public interface CardObserver {
        void onNotify(Card card);
        CardObserver getNext();
        void setNext(CardObserver next);
    }
}
