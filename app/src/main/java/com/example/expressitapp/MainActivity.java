package com.example.expressitapp;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expressitapp.design.GridViewAdapter;
import com.example.expressitapp.design.RecycleViewAdapter;
import com.example.expressitapp.model.Card;
import com.example.expressitapp.model.ImageCard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static String currentFolder = "";
    public static final Stack<String> previousFolders = new Stack<>();
    public static final List<Card> choseCards = new ArrayList<>();

    private ImageView add_card_button;
    private ImageView add_folder_button;
    private TextView toolbar_textview;
    private ImageView backButton;
    private GridView gridView;
    private ImageView show_sentence_button;
    private RecyclerView sentence_view;
    private ImageView play_stop_sentence_button;
    private ImageView delete_chose_card_button;
    private Queue<Card> playingCards;
    private boolean isCardsPlaying = false;
    private Toast previousToast;
    private MediaPlayer previousChoseCardAudio;

    public static final Stack<Context> previousActivities = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add_folder_button = findViewById(R.id.toolbar).findViewById(R.id.toolbar_add_folder_icon);
        toolbar_textview = findViewById(R.id.toolbar).findViewById(R.id.toolbar_textview);
        add_card_button = findViewById(R.id.toolbar).findViewById(R.id.toolbar_add_card_icon);
        gridView = findViewById(R.id.card_grid_view);
        backButton = findViewById(R.id.toolbar).findViewById(R.id.toolbar_back_icon);
        show_sentence_button = findViewById(R.id.toolbar).findViewById(R.id.show_sentence_icon);


        // tool bar text view
        if (currentFolder.equals("")) {
            toolbar_textview.setText("Main folder");
        } else {
            toolbar_textview.setText(currentFolder);
        }
        //===============

        // card_grid_view
        GridViewAdapter gridViewAdapter = new GridViewAdapter(MainActivity.this, Card.getCardList(contextWrapper, currentFolder));

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        //===============

        // add card button
        add_card_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousActivities.push(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, AddNewCardActivity.class);
                startActivity(intent);
            }
        });
        //===============

        // add folder button
        add_folder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousActivities.push(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, AddNewFolderActivity.class);
                startActivity(intent);
            }
        });
        //=================

        // back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!previousActivities.empty()) {
                    currentFolder = previousFolders.pop();
                    Context activity = MainActivity.previousActivities.pop();
                    Intent intent = new Intent(MainActivity.this, activity.getClass());
                    startActivity(intent);
                }
            }
        });
        //=================

        // show sentence button
        show_sentence_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialize dialog
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.activity_bottom_card_sentence_view);

                // sentence
                sentence_view = dialog.findViewById(R.id.sentence_view);
                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                sentence_view.setLayoutManager(horizontalLayoutManager);
                RecycleViewAdapter recycleViewAdapter = new RecycleViewAdapter();
                recycleViewAdapter.setData(choseCards);
                sentence_view.setAdapter(recycleViewAdapter);
                //================

                // delete button
                delete_chose_card_button = dialog.findViewById(R.id.delete_card_button);
                delete_chose_card_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //stop playing cards
                        stopAllPlayingCards();

                        // update
                        if (!choseCards.isEmpty()) {
                            choseCards.remove(choseCards.size() - 1);
                            recycleViewAdapter.setData(choseCards);
                        }
                    }
                });
                //=================

                // play/stop button
                play_stop_sentence_button = dialog.findViewById(R.id.play_stop_button);
                play_stop_sentence_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isCardsPlaying) {
                            play_stop_sentence_button.setImageResource(R.drawable.play);
                            isCardsPlaying = false;

                            // stop all cards
                            stopAllPlayingCards();
                            ;
                        } else {
                            play_stop_sentence_button.setImageResource(R.drawable.stop);
                            isCardsPlaying = true;

                            playingCards = new LinkedList<>(choseCards);
                            playCardAudio(playingCards);
                        }
                    }
                });

                // dialog dismiss
                dialog.setOnDismissListener(dialogInterface -> {
                    stopAllPlayingCards();
                });

                //show dialog
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        });
    }

    private void stopAllPlayingCards() {
        if (playingCards != null && !playingCards.isEmpty()) {
            Card first = playingCards.poll();
            if (first instanceof ImageCard) {
                ImageCard imageCard = (ImageCard) first;
                MediaPlayer mediaPlayer = imageCard.getCardAudio();
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
            playingCards.clear();
        }
    }

    private void playCardAudio(Queue<Card> cardQueue) {
        if (!cardQueue.isEmpty()) {
            Card card = cardQueue.poll();
            if (card instanceof ImageCard) {
                ImageCard imageCard = (ImageCard) card;
                System.out.println(imageCard.getName());
                MediaPlayer mediaPlayer = imageCard.getCardAudio();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playCardAudio(cardQueue);
                    }
                });
            }
        } else {
            play_stop_sentence_button.setImageResource(R.drawable.play);
            isCardsPlaying = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
        Card card = (Card) parent.getItemAtPosition(position);

        if (card instanceof ImageCard) {
            ImageCard imageCard = (ImageCard) card;
//            if(choseCardAudio != null) {
//                if (choseCardAudio.isPlaying()) {
//                    choseCardAudio.stop();
//                    choseCardAudio.reset();
//                }
//            }
//            choseCardAudio = imageCard.getCardAudio();
//            if (choseCardAudio.isPlaying()) {
//                choseCardAudio.stop();
//                choseCardAudio.reset();
//            } else {
//                choseCardAudio.start();
//            }

            MediaPlayer choseCardAudio = imageCard.getCardAudio();
            if (previousChoseCardAudio != null) {
                if(previousChoseCardAudio.isPlaying()) {
                    previousChoseCardAudio.stop();
                    previousChoseCardAudio.seekTo(0);
                }
            }
            previousChoseCardAudio = choseCardAudio;
            choseCardAudio.start();

            choseCards.add(imageCard);

            // show alert
            if (previousToast != null) {
                previousToast.cancel();
            }
            Toast currentToast = Toast.makeText(MainActivity.this, "Added " + imageCard.getName() + " to sentence",
                    Toast.LENGTH_SHORT);
            previousToast = currentToast;
            currentToast.show();

        } else {
            previousFolders.push(currentFolder);
            currentFolder = currentFolder + "/" + card.getName();
            previousActivities.push(MainActivity.this);
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}