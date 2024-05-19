package com.example.expressitapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

public class AddNewCardActivity extends AppCompatActivity {

    private int MICROPHONE_PERMISSION_CODE = 200;
    private int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 123;
    private boolean isRecording = false;
    private boolean isRecorded = false;
    private boolean isCardImageUploaded = false;
    private boolean isCardAudioRecorded = false;

    private ImageView backButton;
    private Button recordButton;
    private Button saveButton;
    private ImageView cardImage;
    private CheckBox converToAudioCheckBox;
    private MediaRecorder mediaRecorder;
    private EditText cardNameText;
    private TextView recordingNotification;
    private TextView saveCardNotification;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_card);
        backButton = findViewById(R.id.toolbar).findViewById(R.id.toolbar_back_icon);
        recordButton = findViewById(R.id.record_voice_button);
        saveButton = findViewById(R.id.save_card_button);
        cardImage = findViewById(R.id.add_image_button);
        cardNameText = findViewById(R.id.card_name_text_field);
        recordingNotification = findViewById(R.id.recording_notification);
        saveCardNotification = findViewById(R.id.save_card_notification);
        converToAudioCheckBox = findViewById(R.id.covert_to_audio_checkbox);
        disableButtons();

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                handleBackButtonClicked();
            }
        };
        AddNewCardActivity.this.getOnBackPressedDispatcher().addCallback(this, callback);

        // back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackButtonClicked();
            }
        });
        //============

        // microphone check permission
        if (isMicrophonePresent()) {
            getMicrophonePermission();
        }
        //============

        // record button
        recordButton.setBackgroundColor(Color.rgb(204, 0, 0)); // dark red
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    converToAudioCheckBox.setEnabled(false);

                    isRecorded = false;
                    recordingNotification.setText("");

                    recordButton.setBackgroundColor(Color.rgb(51, 51, 204)); // dark blue
                    recordButton.setText("RECORDING");
                    isRecording = true;
                    try {
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setOutputFile(createAndGetMP3File(formatCardImageName(cardNameText.getText().toString())));
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    converToAudioCheckBox.setEnabled(true);

                    recordButton.setBackgroundColor(Color.rgb(204, 0, 0));
                    recordButton.setText("RECORD");
                    isRecorded = true;
                    isRecording = false;
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;

//                    isCardAudioRecorded = true;
                    recordingNotification.setText("Card audio recorded");
                }
            }
        });
        //====================

        // save button
        saveButton.setBackgroundColor(Color.rgb(51, 204, 51)); // dark green
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((converToAudioCheckBox.isChecked() && isCardImageUploaded) || (!converToAudioCheckBox.isChecked() && isCardImageUploaded && isRecorded)) {
                    saveCardNotification.setText("");

                    String fileName = saveImageToGallery();
                    if(converToAudioCheckBox.isChecked()) {
                        convertAndSaveAudioFromText(cardNameText.getText().toString(), createWavFile());
                    }
                    if (fileName.equals("")) {
                        // saving success dialog
                        Dialog dialog = new Dialog(AddNewCardActivity.this);
                        dialog.setContentView(R.layout.saving_success_dialog);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setCancelable(false);

                        // dialog buttons and text
                        Button continueButton = dialog.findViewById(R.id.continue_button);
//                        TextView mainStatus = dialog.findViewById(R.id.main_status);
//                        TextView detailStatus = dialog.findViewById(R.id.detail_status);
//                        ImageView statusIcon = dialog.findViewById(R.id.status_icon);
//                        CardView cardView = dialog.findViewById(R.id.dialog_background);

                        continueButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.previousActivities.pop();
                                // switch to main screen
                                Intent intent = new Intent(AddNewCardActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });


                        dialog.show();
                    } else {
                        // saving fail dialog
                        Dialog dialog = new Dialog(AddNewCardActivity.this);
                        dialog.setContentView(R.layout.saving_fail_dialog);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setCancelable(false);

                        // dialog buttons and text
                        Button backButton = dialog.findViewById(R.id.continue_button);
//                        TextView mainStatus = dialog.findViewById(R.id.main_status);
                        TextView detailStatus = dialog.findViewById(R.id.detail_status);
//                        ImageView statusIcon = dialog.findViewById(R.id.status_icon);
//                        CardView cardView = dialog.findViewById(R.id.dialog_background);

                        detailStatus.setText("Card " + fileName + " already exists in your gallary");

                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                } else {
                    saveCardNotification.setText("Please choose a card image and audio");
                }
            }
        });
        //==================

        // choose image
        cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCardImageUploaded = false;
                ImagePicker.with(AddNewCardActivity.this)
                        .cropSquare()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)        //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(220, 220)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });
        //=================

        // card name text field
        cardNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = s.toString();
                if (!text.isEmpty()) {
                    if (isRecorded) {
                        recordingNotification.setText("");
                        isRecorded = false;
                        deleteRecordedAudio(text.toString());
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isCardImageUploaded = false;
                String text = s.toString();
                if (!text.isEmpty()) {
                    enableButtons();
                } else {
                    disableButtons();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //===================

        //convert to audio check box
        converToAudioCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (converToAudioCheckBox.isChecked()) {
                    if (isRecorded) {
                        recordingNotification.setText("");
                        isRecorded = false;
                        deleteRecordedAudio(cardNameText.getText().toString());
                    }
                    recordButton.setEnabled(false);
                } else if (!cardNameText.getText().toString().equals("")) {
                    recordButton.setEnabled(true);
                }
            }
        });
        //===================

        // text to speech
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });
    }

    private void handleBackButtonClicked() {
        if(!MainActivity.previousActivities.empty()) {
            Context activity = MainActivity.previousActivities.pop();
            Intent intent = new Intent(AddNewCardActivity.this, activity.getClass());
            startActivity(intent);
        }
    }


    private String createWavFile() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + MainActivity.currentFolder);
        String fileName = formatCardImageName(cardNameText.getText().toString());
        return musicDir + "/" + fileName + ".wav";
    }

    private void convertAndSaveAudioFromText(String text, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.synthesizeToFile(text, null, new File(filePath), "ttnm");
//            Toast.makeText(AddNewCardActivity.this, filePath, Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> hm = new HashMap<>();
            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ttnm");
            textToSpeech.synthesizeToFile(text, hm, filePath);
//            Toast.makeText(AddNewCardActivity.this, filePath, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecordedAudio(String cardName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + MainActivity.currentFolder);
        String fileName = formatCardImageName(cardName);
        String filePath = musicDir + "/" + fileName + ".mp3";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.delete(Paths.get(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String saveImageToGallery() {
        BitmapDrawable draw = (BitmapDrawable) cardImage.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream;
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File pictureDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + MainActivity.currentFolder);
        String fileName = formatCardImageName(cardNameText.getText().toString());
        String filePath = pictureDir + "/" + fileName + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Files.exists(Paths.get(filePath))) {
                return fileName;
            } else {
                File outFile = new File(filePath);
                try {
                    outStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.out.println(outFile.getPath());
                return "";
            }
        } else return "";
    }

    private String formatCardImageName(String cardName) {
        cardName.trim();
        return cardName.replaceAll("[^\\w\\s]", "_");
    }

    private void enableButtons() {
        saveButton.setEnabled(true);
        cardImage.setEnabled(true);
        cardImage.setImageResource(R.drawable.image);
        if (!converToAudioCheckBox.isChecked()) {
            recordButton.setEnabled(true);
        }
    }

    private void disableButtons() {
        saveButton.setEnabled(false);
        cardImage.setEnabled(false);
        cardImage.setImageResource(R.drawable.blured_image);
        recordButton.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        cardImage.setImageURI(uri);
        isCardImageUploaded = true;
    }

    private boolean isMicrophonePresent() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    private String createAndGetMP3File(String fileName) throws IOException {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + MainActivity.currentFolder);
        File file = new File(musicDir, fileName + ".mp3");
        System.out.println(file.getPath());
        return file.getPath();
    }
}