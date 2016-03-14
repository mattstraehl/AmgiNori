package com.matthias.android.amginori;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matthias.android.amginori.persistence.Anki2DbHelper;

import java.util.ArrayList;
import java.util.List;

public class CardListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.card_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    private void updateUI() {
        Anki2DbHelper database = new Anki2DbHelper(getActivity());
        List<String> list = database.getAllCards();
        List<Card> cards = new ArrayList<>();
        for (String s : list) {
            String[] fields = s.split("\\x1f", -1);
            cards.add(new Card(fields[0], fields[1]));
        }

        if (mAdapter == null) {
            mAdapter = new CardAdapter(cards);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCards(cards);
            mAdapter.notifyDataSetChanged();
        }

        int cardCount = CardLibrary.get(getActivity()).size();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(cardCount + " cards shown");
    }

    private class CardHolder extends RecyclerView.ViewHolder {

        private TextView mFront;
        private TextView mBack;

        private Card mCard;

        public CardHolder(View itemView) {
            super(itemView);

            mFront = (TextView) itemView.findViewById(R.id.list_item_card_front);
            mBack = (TextView) itemView.findViewById(R.id.list_item_card_back);
        }

        public void bindCard(Card card) {
            mCard = card;
            mFront.setText(mCard.mFront);
            mBack.setText(mCard.mBack);
        }
    }

    private class CardAdapter extends RecyclerView.Adapter<CardHolder> {

        private List<Card> mCards;

        public CardAdapter(List<Card> cards) {
            mCards = cards;
        }

        @Override
        public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_card, parent, false);
            return new CardHolder(view);
        }

        @Override
        public void onBindViewHolder(CardHolder holder, int position) {
            Card card = mCards.get(position);
            holder.bindCard(card);
        }

        @Override
        public int getItemCount() {
            return mCards.size();
        }

        public void setCards(List<Card> cards) {
            mCards = cards;
        }
    }
}
