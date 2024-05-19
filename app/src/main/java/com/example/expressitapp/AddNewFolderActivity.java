package com.example.expressitapp;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class AddNewFolderActivity extends AppCompatActivity {
    private ImageView backButton;
    private EditText folderNameTextField;
    private Button saveFolderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_folder);

        backButton = findViewById(R.id.toolbar).findViewById(R.id.toolbar_back_icon);
        folderNameTextField = findViewById(R.id.folder_name_text_field);
        saveFolderButton = findViewById(R.id.save_folder_button);
        TextView textView = findViewById(R.id.toolbar).findViewById(R.id.toolbar_textview);
        textView.setText("Add new folder");


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                handleBackButtonClicked();
            }
        };
        AddNewFolderActivity.this.getOnBackPressedDispatcher().addCallback(this, callback);

        // back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackButtonClicked();
            }
        });
        //================

        //save folder button
        saveFolderButton.setBackgroundColor(Color.rgb(51, 204, 51));
        saveFolderButton.setEnabled(false);
        saveFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDirName = formatFolderName(folderNameTextField.getText().toString());

                if (saveNewFolder(newDirName)) {
                    // saving success dialog
                    Dialog dialog = new Dialog(AddNewFolderActivity.this);
                    dialog.setContentView(R.layout.saving_success_dialog);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(false);

                    // dialog buttons and text
                    Button continueButton = dialog.findViewById(R.id.continue_button);
//                        TextView mainStatus = dialog.findViewById(R.id.main_status);
                    TextView detailStatus = dialog.findViewById(R.id.detail_status);
                    detailStatus.setText("Folder " + newDirName + " has been created");
//                        ImageView statusIcon = dialog.findViewById(R.id.status_icon);
//                        CardView cardView = dialog.findViewById(R.id.dialog_background);

                    continueButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.previousActivities.pop();
                            // switch to main screen
                            Intent intent = new Intent(AddNewFolderActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });


                    dialog.show();
                } else {
                    // saving fail dialog
                    Dialog dialog = new Dialog(AddNewFolderActivity.this);
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

                    detailStatus.setText("Folder " + newDirName + " already exists in your gallary");

                    backButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            }
        });
        //==============

        // folder name text field
        folderNameTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (!text.isEmpty()) {
                    saveFolderButton.setEnabled(true);
                } else {
                    saveFolderButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void handleBackButtonClicked() {
        if(!MainActivity.previousActivities.empty()) {
            Context activity = MainActivity.previousActivities.pop();
            Intent intent = new Intent(AddNewFolderActivity.this, activity.getClass());
            startActivity(intent);
        }
    }

    private boolean saveNewFolder(String dirName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File newPictureDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + MainActivity.currentFolder + "/" + dirName);
        File newMusicDir = new File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + MainActivity.currentFolder + "/" + dirName);
        return newPictureDir.mkdirs() && newMusicDir.mkdirs();
    }

    private String formatFolderName(String cardName) {
        cardName.trim();
        return cardName.replaceAll("[^\\w\\s]", "_");
    }
}