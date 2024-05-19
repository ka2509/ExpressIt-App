package com.example.expressitapp.model;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;

import java.net.URI;

public class ImageCard extends Card {

    private Bitmap imageBitmap;
    private MediaPlayer cardAudio;

    public ImageCard(MediaPlayer cardAudio, Bitmap imageBitmap, String name, String path) {
        super(name, path);
        this.imageBitmap = imageBitmap;
        this.cardAudio = cardAudio;
    }

    public String getName() {
        return super.getName();
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public MediaPlayer getCardAudio() {
        return cardAudio;
    }

    public void setCardAudio(MediaPlayer cardAudio) {
        this.cardAudio = cardAudio;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
