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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText email, password;
    private Button loginButton, signupButton;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        FirebaseApp.initializeApp(this);

        loadRememberedUser();

        loginButton.setOnClickListener(v -> loginUser());

        signupButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
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
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            saveFcmToken(user);
                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (!documentSnapshot.exists()) {
                                            createUserDocument(userId, userEmail);
                                        }
                                        startActivity(new Intent(MainActivity.this, JobsActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to check user document: " + e.getMessage());
                                        Toast.makeText(MainActivity.this, "Error checking user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: User is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });
    }

    private void saveFcmToken(FirebaseUser user) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                String userId = user.getUid();
                db.collection("users").document(userId)
                        .set(new HashMap<String, Object>() {{
                            put("fcmToken", token);
                        }}, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved for user: " + userId))
                        .addOnFailureListener(e -> Log.w(TAG, "Failed to save FCM token: " + e.getMessage(), e));
            } else {
                Log.w(TAG, "Failed to get FCM token", task.getException());
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
        Log.d(TAG, "Creating user document for " + userId);

        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created successfully");
                    Toast.makeText(MainActivity.this, "User document created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error creating user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}