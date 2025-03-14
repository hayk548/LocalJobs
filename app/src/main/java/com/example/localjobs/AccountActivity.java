package com.example.localjobs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private Uri profileImageUri;
    private Uri resumeUri;
    private EditText editTextUsername, editTextNewPassword;
    private Button btnSaveUsername, btnChangePassword, btnUploadImage, btnUploadResume;
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadResume = findViewById(R.id.btnUploadResume);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        String usernameFromIntent = getIntent().getStringExtra("username");

        if (usernameFromIntent != null && !usernameFromIntent.isEmpty()) {
            editTextUsername.setText(usernameFromIntent);
        } else {
            loadUserData();
        }

        btnSaveUsername.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (!TextUtils.isEmpty(username)) {
                updateUsername(username);
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            if (!TextUtils.isEmpty(newPassword) && newPassword.length() >= 6) {
                changePassword(newPassword);
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            editTextUsername.setText(username);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show());
    }

    private void updateUsername(String username) {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .update("username", username)
                .addOnSuccessListener(aVoid -> Toast.makeText(AccountActivity.this, "Username updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error updating username", Toast.LENGTH_SHORT).show());
    }

    private void changePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> Toast.makeText(AccountActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error changing password", Toast.LENGTH_SHORT).show());
    }
}