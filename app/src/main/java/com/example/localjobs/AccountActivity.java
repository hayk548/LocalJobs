package com.example.localjobs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class AccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_RESUME_REQUEST = 2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private Uri profileImageUri;
    private Uri resumeUri;
    private TextView username;
    private EditText editTextUsername, editTextNewPassword;
    private Button btnSaveUsername, btnChangePassword, btnUploadImage, btnUploadResume;
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = mAuth.getCurrentUser().getUid();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadResume = findViewById(R.id.btnUploadResume); // Add button to upload resume
        imageViewProfile = findViewById(R.id.imageViewProfile);

        // Check if user is logged in
        if (currentUser != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }

        // Set username
        btnSaveUsername.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            if (!TextUtils.isEmpty(username)) {
                updateUsername(username);
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Change password
        btnChangePassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            if (!TextUtils.isEmpty(newPassword) && newPassword.length() >= 6) {
                changePassword(newPassword);
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload profile image
        btnUploadImage.setOnClickListener(v -> openImageChooser());

        // Upload resume
        btnUploadResume.setOnClickListener(v -> openResumeChooser());
    }

    // Load user data (username and profile image URL) from Firestore
    private void loadUserData() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        editTextUsername.setText(username);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Load image using Picasso or any image loading library
                            Picasso.get().load(imageUrl).into(imageViewProfile);
                        }
                    } else {
                        createUserDocument(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AccountActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
    }

    private void createUserDocument(String userId) {
        db.collection("users").document(userId)
                .set(new User(userId, "Username", ""))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AccountActivity.this, "User document created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AccountActivity.this, "Error creating user document", Toast.LENGTH_SHORT).show();
                });
    }

    // Update username in Firestore
    private void updateUsername(String username) {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .update("username", username)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AccountActivity.this, "Username updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error updating username", Toast.LENGTH_SHORT).show());
    }

    // Change password in Firebase Authentication
    private void changePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AccountActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error changing password", Toast.LENGTH_SHORT).show());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle result from image chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            imageViewProfile.setImageURI(profileImageUri);

            // Upload the image to Firebase Storage
            uploadProfileImage();
        }

        // Handle resume chooser result
        if (requestCode == PICK_RESUME_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            resumeUri = data.getData();
            uploadResume();
        }
    }

    // Upload profile image to Firebase Storage
    private void uploadProfileImage() {
        if (profileImageUri != null) {
            StorageReference profileImageRef = storage.getReference("profile_images/" + currentUser.getUid() + ".jpg");
            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the uploaded image URL
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Save the image URL to Firestore
                            db.collection("users").document(currentUser.getUid())
                                    .update("profileImage", imageUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AccountActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error updating profile image", Toast.LENGTH_SHORT).show());
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show());
        }
    }

    // Open resume chooser (PDF or DOCX files)
    private void openResumeChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // You can change this type to support other formats (e.g., docx)
        startActivityForResult(intent, PICK_RESUME_REQUEST);
    }

    // Upload resume to Firebase Storage
    private void uploadResume() {
        if (resumeUri != null) {
            StorageReference resumeRef = storage.getReference("resumes/" + currentUser.getUid() + "_resume.pdf");
            resumeRef.putFile(resumeUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        resumeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String resumeUrl = uri.toString();

                            // Save the resume URL to Firestore
                            db.collection("users").document(currentUser.getUid())
                                    .update("resume", resumeUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AccountActivity.this, "Resume uploaded successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error uploading resume", Toast.LENGTH_SHORT).show());
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error uploading resume", Toast.LENGTH_SHORT).show());
        }
    }
}
