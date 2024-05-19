package com.example.expressitapp.model;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Card {

    private String name;

    private String path;

    public Card(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ArrayList<Card> getCardList(ContextWrapper contextWrapper, String currentFolder) {
        ArrayList<Card> cards = new ArrayList<>();

        File musicDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + currentFolder);

        File[] musicFiles = musicDir.listFiles();
        if (musicFiles != null) {

            for (File file : musicFiles) {
                if (file.isDirectory()) {
                    String name = file.getName();
                    String path = file.getPath();
                    cards.add(new Card(name, path));
                } else if (file.isFile() && (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav"))) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(file.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.prepareAsync();

                    String name = file.getName().replace(".mp3", "").replace(".wav", "");
                    String imagePath = file.getPath().replace(Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_PICTURES)
                            .replace(".mp3", ".jpg").replace(".wav", ".jpg");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (Files.exists(Paths.get(imagePath))) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                            ImageCard imageCard = new ImageCard(mediaPlayer, bitmap, name, imagePath);
                            cards.add(imageCard);
                        }
                        }
                    }
            }
        }

        return cards;
    }

    @Override
    public String toString() {
        return name;
    }
}
