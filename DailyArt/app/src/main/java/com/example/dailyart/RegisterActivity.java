package com.example.dailyart;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_GALLERY = 2;

    private EditText username;
    private ImageView imgUser;
    private EditText password;
    private EditText email;
    private EditText birthday;
    private Button reg;
    private Button btnSelectPhoto;
    private CalendarView calendarView;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference dbRef;

    private String date;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        username = findViewById(R.id.tvUsernameRegister);
        imgUser = findViewById(R.id.imgUser);
        password = findViewById(R.id.tvPasswordRegister);
        email = findViewById(R.id.tvEmailRegister);
        birthday = findViewById(R.id.tvBirthday);
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


        reg = findViewById(R.id.btnRegister);
        reg.setOnClickListener(view -> register());

        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSelectPhoto.setOnClickListener(v -> selectPhoto());
    }

    private void register(){
        String userText = username.getText().toString();
        String pswText = password.getText().toString();
        String emailText = email.getText().toString();
        String bdayText = birthday.getText().toString();

        if(userText.isEmpty() || pswText.isEmpty() || emailText.isEmpty() || bdayText.isEmpty()){
            Toast.makeText(getBaseContext(), "CAMPI VUOTI!", Toast.LENGTH_SHORT).show();
        }
        else if(!emailText.contains("@")){
            Toast.makeText(getBaseContext(), "Inserire un'email esistente", Toast.LENGTH_SHORT).show();
        }
        else {
            dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(userText)){
                        Toast.makeText(getBaseContext(), "USERNAME GIA' ESISTENTE!", Toast.LENGTH_SHORT).show();
                    } else {
                        dbRef.child("User").child(userText).child("password").setValue(pswText);
                        dbRef.child("User").child(userText).child("email").setValue(emailText);
                        dbRef.child("User").child(userText).child("birthday").setValue(bdayText);
                        uploadPicture();
                        Toast.makeText(getApplicationContext(), "Account creato con successo!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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
                imgUser.setImageURI(imageUri);

            }
            if (requestCode == PERMISSION_CAMERA) {
                Bundle photo = data.getExtras();
                Bitmap bitmap = (Bitmap) photo.get("data");
                imgUser.setImageBitmap(bitmap);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null);

                imageUri = Uri.parse(path);
            }
        }
    }

    private void uploadPicture() {
        try {
            StorageReference riversRef = storageReference.child("image/" + username.getText().toString() + ".jpeg");
            riversRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {})
                    .addOnFailureListener(e -> {});
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}