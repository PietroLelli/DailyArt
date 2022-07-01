package com.example.dailyart;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    static String loggedUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        TextView register = findViewById(R.id.tvRegister);
        register.setOnClickListener(view -> openRegister());

        Button btn = findViewById(R.id.btnLogin);
        btn.setOnClickListener(view -> login());
    }

    public void openRegister(){
        Intent i = new Intent();
        ComponentName componentName = new ComponentName(getApplicationContext(), RegisterActivity.class);
        i.setComponent(componentName);
        startActivity(i);
    }

    public void login() {
        EditText username = findViewById(R.id.tvUsername);
        String userText = username.getText().toString();

        EditText password = findViewById(R.id.tvPassword);
        String pswText = password.getText().toString();

        if (userText.isEmpty() || pswText.isEmpty()){
            Toast.makeText(getBaseContext(), "USERNAME O PASSWORD NON INSERITI!", Toast.LENGTH_SHORT).show();
        } else {
            dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(userText)){
                        final String getPsw = snapshot.child(userText).child("password").getValue(String.class);
                        if (getPsw.equals(pswText)) {
                            loggedUser = userText;
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getBaseContext(), "PASSWORD NON CORRETTA!", Toast.LENGTH_SHORT).show();
                            password.setText("");
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "USERNAME NON CORRETTO!", Toast.LENGTH_SHORT).show();
                        username.setText("");
                        password.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}