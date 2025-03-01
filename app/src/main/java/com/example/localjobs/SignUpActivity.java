package com.example.localjobs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password;
    private Button signupButton;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signupButton = findViewById(R.id.signupButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loadRememberedUser();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rememberMeCheckbox.isChecked()) {
            saveUserEmail(userEmail);
        } else {
            clearSavedEmail();
        }

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, WaitingForEmailActivity.class);
                                                finish();
                                                setContentView(R.layout.activity_waiting_for_email);
                                                startActivity(intent);
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadRememberedUser() {
        String savedEmail = sharedPreferences.getString("savedEmail", "");
        if (!savedEmail.isEmpty()) {
            email.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
        }
    }
}
