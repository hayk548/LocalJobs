package com.example.localjobs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeleteAccountActivity extends AppCompatActivity {

    private Button btnConfirmDelete, btnCancel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnConfirmDelete = findViewById(R.id.btnConfirmDelete);
        btnCancel = findViewById(R.id.btnCancel);

        btnConfirmDelete.setOnClickListener(v -> deleteAccount());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Delete user data from Firestore first
            db.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Then delete the authentication account
                        user.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(DeleteAccountActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}