package com.example.localjobs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AccountActivity extends AppCompatActivity {

    private TextView userNameSurname, userEmail, userPhoneNumber;
    private Button btnLogout;
    private ImageView changePassword, profilePicture, deleteAccount;
    private FirebaseFirestore db;
    private ListenerRegistration profileListener;

    // Launcher to pick an image
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadImageToFirestore(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        initializeViews();

        db = FirebaseFirestore.getInstance();

        // Set up profile picture change
        profilePicture.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        deleteAccount.setOnClickListener(v -> startActivity(new Intent(AccountActivity.this, DeleteAccountActivity.class)));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            setupProfileListener(firebaseUser);
            fetchUserData(firebaseUser);
            loadUserProfile(firebaseUser);
        }

        // Change password action
        changePassword.setOnClickListener(v -> startActivity(new Intent(AccountActivity.this, ChangePasswordActivity.class)));

        // Logout action
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            clearSharedPreferences();
            startActivity(new Intent(AccountActivity.this, MainActivity.class));
            finish();
            Toast.makeText(AccountActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.profilePicture);
        changePassword = findViewById(R.id.changePassword);
        userNameSurname = findViewById(R.id.UserNameSurname);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        userEmail = findViewById(R.id.UserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        deleteAccount = findViewById(R.id.deleteAccount);
    }

    private void fetchUserData(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userNameSurname.setText(documentSnapshot.getString("name"));
                        userPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                    }
                })
                .addOnFailureListener(e -> Log.e("AccountActivity", "Failed to fetch user data", e));

        userEmail.setText(firebaseUser.getEmail());
    }

    private void loadUserProfile(FirebaseUser firebaseUser) {
        Uri photoUrl = firebaseUser.getPhotoUrl();
        Glide.with(this)
                .load(photoUrl != null ? photoUrl : R.drawable.profile_picture)
                .into(profilePicture);
    }

    private void setupProfileListener(FirebaseUser firebaseUser) {
        profileListener = db.collection("users").document(firebaseUser.getUid())
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e == null && documentSnapshot != null && documentSnapshot.exists()) {
                        userNameSurname.setText(documentSnapshot.getString("name"));
                        userPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                        loadProfileImage(documentSnapshot.getString("profileImage"));
                    }
                });
    }

    private void loadProfileImage(String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(this)
                    .load(decodedByte)
                    .transform(new CircleCrop())
                    .into(profilePicture);
        }
    }

    private void uploadImageToFirestore(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();

                if (imageBytes.length > 900000) {
                    Toast.makeText(this, "Image too large, please select a smaller image", Toast.LENGTH_SHORT).show();
                    return;
                }

                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                db.collection("users").document(user.getUid())
                        .update("profileImage", base64Image)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Log.e("AccountActivity", "Failed to upload image to Firestore", e));
            } catch (Exception e) {
                Log.e("AccountActivity", "Error processing image", e);
            }
        }
    }

    private void clearSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }
}
