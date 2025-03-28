package com.example.localjobs;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private Uri profileImageUri;

    private EditText editTextUsername, editTextNewPassword;
    private Button btnSaveUsername, btnChangePassword, btnUploadImage, btnLogout;
    private ImageView imageViewProfile;

    private JobAdapter appliedJobAdapter;
    private List<Job> appliedJobsList = new ArrayList<>();  // Initialize with empty list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue)); // Set status bar color
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize RecyclerView and Adapter
        appliedJobAdapter = new JobAdapter(appliedJobsList, this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData();

        if (currentUser != null) {
            loadAppliedJobs();
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

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnUploadImage.setOnClickListener(v -> openFileChooser());
    }

    private void loadAppliedJobs() {
        String currentUserId = currentUser.getUid();

        // Query the Firestore 'applications' collection for the jobs applied by the current user
        db.collection("applications")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> appliedJobIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String jobId = document.getString("jobId");
                            appliedJobIds.add(jobId);
                        }
                        fetchJobDetails(appliedJobIds);
                    } else {
                        Toast.makeText(AccountActivity.this, "Error loading applied jobs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchJobDetails(List<String> appliedJobIds) {
        if (appliedJobIds.isEmpty()) {
            return;
        }
        db.collection("jobs")
                .whereIn("jobId", appliedJobIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Job> jobList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Job job = document.toObject(Job.class);
                            jobList.add(job);
                        }

                        // Clear the old list and add new data
                        appliedJobsList.clear();
                        appliedJobsList.addAll(jobList);
                        appliedJobAdapter.notifyDataSetChanged();  // Notify the adapter to update RecyclerView
                    } else {
                        Toast.makeText(AccountActivity.this, "Error fetching job details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            imageViewProfile.setImageURI(profileImageUri);

            // Upload image to Firebase (only if you still want to upload)
            uploadImageToFirebase();

            // Save image locally in internal storage
            saveImageLocally(profileImageUri);
        }
    }

    private void uploadImageToFirebase() {
        if (profileImageUri == null) return;

        String userId = currentUser.getUid();
        StorageReference fileReference = storage.getReference("profile_images/" + userId + ".jpg");

        fileReference.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Ensure the file exists before getting the URL
                    fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                saveImageUrlToFirestore(uri.toString());
                            })
                            .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("profileImage", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AccountActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(imageUrl).into(imageViewProfile);
                })
                .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error saving image", Toast.LENGTH_SHORT).show());
    }

    private void saveImageLocally(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Define the path to save the image in the internal storage
            File file = new File(getFilesDir(), "profile_image.jpg");

            // Create an output stream to save the image to the file
            FileOutputStream outputStream = new FileOutputStream(file);

            // Copy the input stream (image) to the output stream
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Optionally, save the file path to SharedPreferences for future use
            saveImagePathToPreferences(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImagePathToPreferences(String imagePath) {
        getSharedPreferences("userPrefs", MODE_PRIVATE)
                .edit()
                .putString("profileImage", imagePath)
                .apply();
    }

    private void loadUserData() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String profileImage = documentSnapshot.getString("profileImage");

                        if (username != null) {
                            editTextUsername.setText(username);
                        }

                        if (profileImage != null && !profileImage.isEmpty()) {
                            Picasso.get().load(profileImage).into(imageViewProfile);
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
