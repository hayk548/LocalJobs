package com.example.localjobs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton, signupButton;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private String userId;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue)); // Set status bar color
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loadRememberedUser();
        FirebaseApp.initializeApp(this);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(userId)
                        .set(new HashMap<String, Object>() {{
                            put("fcmToken", token);
                        }}, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved"))
                        .addOnFailureListener(e -> Log.w("FCM", "Token save failed", e));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rememberMeCheckbox.isChecked()) {
            saveUserEmail(userEmail);
            saveUserPassword(userPassword);
        } else {
            clearSavedEmail();
            clearSavedPassword();
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            userId = mAuth.getCurrentUser().getUid();
                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Intent intent = new Intent(MainActivity.this, JobsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            createUserDocument(userId, userEmail);
                                            Intent intent = new Intent(MainActivity.this, JobsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            Intent intent = new Intent(MainActivity.this, JobsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", email);
        editor.apply();
    }

    private void clearSavedEmail() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedEmail");
        editor.apply();
    }
    private void saveUserPassword(String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedPassword", password);
        editor.apply();
    }

    private void clearSavedPassword() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedPassword");
        editor.apply();
    }

    private void loadRememberedUser() {
        String savedEmail = sharedPreferences.getString("savedEmail", "");
        String savedPassword = sharedPreferences.getString("savedPassword", "");
        if (!savedEmail.isEmpty()) {
            email.setText(savedEmail);
            password.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }
    }
    private void createUserDocument(String userId, String email) {
        User newUser = new User(userId, email, "");
        Log.d("MainActivity", "Creating user document for " + userId);

        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MainActivity", "User document created successfully.");
                    Toast.makeText(MainActivity.this, "User document created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error creating user document: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error creating user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
