package com.matthias.android.amginori;

import java.util.ArrayList;
import java.util.List;

public final class CardLibrary {

    private static List<Card> sCards;

    public static ArrayList<Card> getRandomCards(int size) {
        if (sCards == null) {
            init();
        }
        ArrayList<Card> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = sCards.get((int) (sCards.size() * Math.random()));
            result.add(card);
            result.add(new Card(card.mFront, card.mBack, !card.mShowFront));
        }
        return result;
    }

    private static void init() {
        sCards = new ArrayList<>();
        sCards.add(new Card("blue", "파란"));
        sCards.add(new Card("body", "몸"));
        sCards.add(new Card("book", "책"));
        sCards.add(new Card("bookstore", "서점"));
        sCards.add(new Card("bowl", "그릇"));
        sCards.add(new Card("bridge", "다리"));
        sCards.add(new Card("ear", "귀"));
        sCards.add(new Card("early", "일찍"));
        sCards.add(new Card("earth", "지구"));
        sCards.add(new Card("food", "음식"));
        sCards.add(new Card("foot", "발"));
        sCards.add(new Card("football", "축구"));
        sCards.add(new Card("light", "불"));
        sCards.add(new Card("lion", "사자"));
        sCards.add(new Card("man", "남자"));
        sCards.add(new Card("map", "지도"));
        sCards.add(new Card("market", "시장"));
        sCards.add(new Card("meal", "식사"));
        sCards.add(new Card("moon", "달"));
        sCards.add(new Card("mountain", "산"));
        sCards.add(new Card("mouth", "입"));
        sCards.add(new Card("movie", "영화"));
        sCards.add(new Card("singer", "가수"));
        sCards.add(new Card("skirt", "치마"));
        sCards.add(new Card("snack", "간식"));
        sCards.add(new Card("snake", "뱀"));
        sCards.add(new Card("solala", "그냥 그래요"));
        sCards.add(new Card("sometimes", "가끔, 때때로"));
        sCards.add(new Card("son", "아들"));
        sCards.add(new Card("soon", "금방"));
        sCards.add(new Card("spider", "거미"));
        sCards.add(new Card("season", "계절"));
        sCards.add(new Card("sentence", "문장"));
        sCards.add(new Card("shape", "모양"));
        sCards.add(new Card("sign", "서명"));
        sCards.add(new Card("skin", "피부"));
        sCards.add(new Card("smell", "향"));
        sCards.add(new Card("sound", "소리"));
        sCards.add(new Card("spring", "봄"));
        sCards.add(new Card("stamp", "도장"));
        sCards.add(new Card("suddenly", "갑자기"));
        sCards.add(new Card("summer", "여름"));
        sCards.add(new Card("sun", "해"));
        sCards.add(new Card("taste", "맛"));
    }
}
