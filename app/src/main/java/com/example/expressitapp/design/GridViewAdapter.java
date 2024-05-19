package com.example.expressitapp.design;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expressitapp.R;
import com.example.expressitapp.model.Card;
import com.example.expressitapp.model.ImageCard;

import java.util.ArrayList;
import java.util.Calendar;

public class GridViewAdapter extends ArrayAdapter<Card> {
    public GridViewAdapter(@NonNull Context context, ArrayList<Card> cards) {
        super(context, 0, R.id.card_name, cards);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        HolderView holderView;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card, parent, false);
            holderView = new HolderView(convertView);
            convertView.setTag(holderView);
        }
        else {
            holderView = (HolderView) convertView.getTag();
        }

        Card card = getItem(position);
        if(card instanceof ImageCard) {
            ImageCard imageCard = (ImageCard) card;
            holderView.icon.setImageBitmap(imageCard.getImageBitmap());
            holderView.text.setText(imageCard.getName());
        } else {
            holderView.icon.setImageResource(R.drawable.small_folder_v2);
            holderView.text.setText(card.getName());
            holderView.background.setBackgroundColor(Color.rgb(255, 195, 0));
        }

        return super.getView(position, convertView, parent);
    }

    private class HolderView {
        private final ImageView icon;
        private final TextView text;
        private final LinearLayout background;

        private HolderView(View view) {
            this.icon = view.findViewById(R.id.card_icon);
            this.text = view.findViewById(R.id.card_name);
            this.background = view.findViewById(R.id.background_layout);
        }
    }
}
