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

    private transient CardObserver mHead;

    public Card(String front, String back) {
        this(front, back, Math.random() < 0.5 ? true : false, CardState.ACTIVE);
    }

    public Card(String front, String back, boolean showFront) {
        this(front, back, showFront, CardState.ACTIVE);
    }

    private Card(String front, String back, boolean showFront, CardState cardState) {
        mFront = front;
        mBack = back;
        mShowFront = showFront;
        mToDisplay = mShowFront ? mFront : mBack;
        mCardState = cardState;
    }

    public boolean match(Card other) {
        return isEnabled() && other.isEnabled() && this.equals(other);
    }

    public Card active() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.ACTIVE);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card marked() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.MARKED);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card disabled() {
        Card result = new Card(mFront, mBack, mShowFront, CardState.DISABLED);
        result.mHead = mHead;
        notifyObservers(result);
        return result;
    }

    public Card copy() {
        return new Card(mFront, mBack, mShowFront, mCardState);
    }

    public Card reversedCopy() {
        return new Card(mFront, mBack, !mShowFront, mCardState);
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
