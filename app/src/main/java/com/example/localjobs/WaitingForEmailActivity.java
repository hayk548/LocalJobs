package com.example.localjobs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WaitingForEmailActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button resendEmailButton, checkVerificationButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_email);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue)); // Set status bar color
        }

        timerTextView = findViewById(R.id.timerTextView);
        resendEmailButton = findViewById(R.id.resendEmailButton);
        checkVerificationButton = findViewById(R.id.checkVerificationButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startTimer();

        checkVerificationButton.setOnClickListener(v -> checkEmailVerification());

        resendEmailButton.setOnClickListener(v -> {
            resendEmailButton.setEnabled(false);
            startTimer();
            resendVerificationEmail();
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Resend available in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                resendEmailButton.setEnabled(true);
                timerTextView.setText("You can now resend email.");
            }
        }.start();
    }

    private void checkEmailVerification() {
        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                Toast.makeText(this, "Email Verified!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Email Not Verified. Please check your inbox.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationEmail() {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to resend email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
