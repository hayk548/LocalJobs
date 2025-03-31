package com.example.localjobs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword;
    private Button btnUpdatePassword, btnCancel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnCancel = findViewById(R.id.btnCancel);

        btnUpdatePassword.setOnClickListener(v -> updatePassword());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updatePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}