package com.example.expressitapp.design;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expressitapp.R;
import com.example.expressitapp.model.Card;
import com.example.expressitapp.model.ImageCard;

import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.HolderView> {

    private List<Card> cardsInSentence;
    public void setData(List<Card> cards) {
        this.cardsInSentence = cards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new HolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderView holder, int position) {
        Card card = cardsInSentence.get(position);

        if(card instanceof ImageCard) {
            ImageCard imageCard = (ImageCard) card;
            holder.icon.setImageBitmap(imageCard.getImageBitmap());
            holder.text.setText(imageCard.getName());
        } else {
            holder.icon.setImageResource(R.drawable.small_folder);
            holder.text.setText(card.getName());
            holder.text.setTextColor(Color.rgb(255, 195, 0));
        }
    }

    @Override
    public int getItemCount() {
        if (cardsInSentence != null) {
            return cardsInSentence.size();
        }
        return 0;
    }

    public class HolderView extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView text;

        public HolderView(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.card_icon);
            this.text = itemView.findViewById(R.id.card_name);
        }
    }
}
