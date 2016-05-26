package com.matthias.android.amginori.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.matthias.android.amginori.Card;
import com.matthias.android.amginori.CardLibrary;
import com.matthias.android.amginori.R;
import com.matthias.android.amginori.persistence.SharedPreferencesHelper;

import java.util.List;

public class CardListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private Toast mToast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.card_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        updateUI();

        return view;
    }

    private void updateUI() {
        List<Card> cards = CardLibrary.get(getActivity()).getAllCards();
        if (mAdapter == null) {
            mAdapter = new CardAdapter(cards);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCards(cards);
            mAdapter.notifyDataSetChanged();
        }
        int librarySize = CardLibrary.get(getActivity()).size();
        String cardsShown = getResources().getQuantityString(R.plurals.numberOfCardsShown, librarySize, librarySize);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(cardsShown);
    }

    private class CardHolder extends RecyclerView.ViewHolder {

        private TextView mFront;
        private TextView mBack;
        private ImageView mDeleteIcon;

        private Card mCard;

        public CardHolder(View itemView) {
            super(itemView);
            mFront = (TextView) itemView.findViewById(R.id.list_item_card_front);
            mBack = (TextView) itemView.findViewById(R.id.list_item_card_back);
            mDeleteIcon = (ImageView) itemView.findViewById(R.id.list_item_delete_icon);
            mDeleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardLibrary.get(getActivity()).deleteCard(mCard);
                    updateUI();
                    SharedPreferencesHelper.get(getActivity()).remove("SavedGameValid");
                    mToast.setText(getString(R.string.text_card_deleted, mCard.mFront, mCard.mBack));
                    mToast.show();
                }
            });
        }

        public void bindCard(Card card) {
            mCard = card;
            mFront.setText(mCard.mFront);
            mBack.setText(mCard.mBack);
            mDeleteIcon.setVisibility(mCard.mId == null ? View.GONE : View.VISIBLE);
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
