package com.example.dailyart;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class UserSettingsActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_GALLERY = 2;

    private DatabaseReference dbRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri imageUri;

    private ImageView imgProfilePhoto;
    private TextView email;
    private TextView passwordOld;
    private TextView passwordNew;
    private EditText birthday;
    private Button btnUpdate;

    private String newPassword;
    private String oldPassword;

    private boolean changed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        storage = FirebaseStorage.getInstance();

        email = findViewById(R.id.tvEmailUpdate);
        passwordOld = findViewById(R.id.tvPasswordUpdateOld);
        passwordNew = findViewById(R.id.tvPasswordUpdateNew);
        birthday = findViewById(R.id.tvBirthdayUpdate);
        birthday.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2022)?2000:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    birthday.setText(current);
                    birthday.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        });

        imgProfilePhoto = findViewById(R.id.imgProfilePhotoUpdate);
        try {
            storageReference = storage.getReference().child("image/" + LoginActivity.loggedUser + ".jpeg");

            File file = File.createTempFile(LoginActivity.loggedUser, "jpeg");
            storageReference.getFile(file).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imgProfilePhoto.setImageBitmap(bitmap);
            });
        } catch (Exception e){
            e.getMessage();
        }

        imgProfilePhoto.setOnClickListener(view -> selectPhoto());

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(view -> updateData());
    }

    private void updateData() {
        String emailTxt = email.getText().toString();
        String passwordOldTxt = passwordOld.getText().toString();
        String passwordNewTxt = passwordNew.getText().toString();
        String birthdayTxt = birthday.getText().toString();


        uploadPicture();

        if(emailTxt.isEmpty() && passwordNewTxt.isEmpty() && passwordOldTxt.isEmpty() && birthdayTxt.isEmpty() && imageUri == null){
            Toast.makeText(getBaseContext(), "Non hai modificato nessun dato!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!emailTxt.isEmpty()) {
                if (emailTxt.contains("@")) {
                    changed = true;
                }
            }
            if (!birthdayTxt.isEmpty()) {
                changed = true;
            }


            dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!emailTxt.isEmpty()) {
                        if (emailTxt.contains("@")) {
                            dbRef.child("User").child(LoginActivity.loggedUser).child("email").setValue(emailTxt);
                            Toast.makeText(getBaseContext(), "EMAIL AGGIORNATA!", Toast.LENGTH_SHORT).show();
                            email.setText("");
                        } else {
                            Toast.makeText(getBaseContext(), "Email non modificata, inserirne una esistente!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!birthdayTxt.isEmpty()) {
                        dbRef.child("User").child(LoginActivity.loggedUser).child("birthday").setValue(birthdayTxt);
                        Toast.makeText(getBaseContext(), "DATA DI NASCITA AGGIORNATA!", Toast.LENGTH_SHORT).show();
                        birthday.setText("");
                    }
                    if ((passwordOldTxt.isEmpty() || passwordNewTxt.isEmpty()) && (!passwordOldTxt.isEmpty() || !passwordNewTxt.isEmpty())) {
                        Toast.makeText(getBaseContext(), "INSERIRE LA VECCHIA E NUOVA PASSWORD!", Toast.LENGTH_SHORT).show();
                    } else if (!passwordOldTxt.isEmpty() && !passwordNewTxt.isEmpty()) {
                        dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                oldPassword = snapshot.child(LoginActivity.loggedUser).child("password").getValue(String.class);
                                if (oldPassword.equals(passwordOldTxt)) {
                                    dbRef.child("User").child(LoginActivity.loggedUser).child("password").setValue(passwordNewTxt);
                                    Toast.makeText(getBaseContext(), "PASSWORD AGGIORNATA!", Toast.LENGTH_SHORT).show();
                                    oldPassword = "";
                                    passwordOld.setText("");
                                    passwordNew.setText("");
                                    if(!changed) {
                                        Intent i = new Intent();
                                        ComponentName componentName = new ComponentName(getApplicationContext(), MainActivity.class);
                                        i.setComponent(componentName);
                                        startActivity(i);
                                    }
                                } else {
                                    Toast.makeText(getBaseContext(), "VECCHIA PASSWORD NON CORRETTA!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if(changed){
                Intent i = new Intent();
                ComponentName componentName = new ComponentName(this, MainActivity.class);
                i.setComponent(componentName);
                startActivity(i);
            }

        }
    }

    private void requestPermissionPhoto(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
    }

    private void requestPermissionGallery(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_GALLERY);
    }

    private void selectPhoto() {
        String options[] = {"Camera", "Galleria"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleziona foto da: ");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionPhoto();
                } else {
                    pickFromCamera();
                }
            } else if (which == 1) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionGallery();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromCamera(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, PERMISSION_CAMERA);
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PERMISSION_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PERMISSION_GALLERY) {
                imageUri = data.getData();
                imgProfilePhoto.setImageURI(imageUri);

            }
            if (requestCode == PERMISSION_CAMERA) {
                Bundle photo = data.getExtras();
                Bitmap bitmap = (Bitmap) photo.get("data");
                imgProfilePhoto.setImageBitmap(bitmap);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null);

                imageUri = Uri.parse(path);
            }
        }
    }

    private void uploadPicture() {
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("image/" + LoginActivity.loggedUser + ".jpeg");
        if(imageUri != null) {
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {})
                    .addOnFailureListener(e -> {});
            if(!changed){
                Intent i = new Intent();
                ComponentName componentName = new ComponentName(this, MainActivity.class);
                i.setComponent(componentName);
                startActivity(i);
            }
        }


    }
}
