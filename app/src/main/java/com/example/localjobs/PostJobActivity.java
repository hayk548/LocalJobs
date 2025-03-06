package com.example.localjobs;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PostJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobDescription, jobLocation, jobDate;
    private Button postJobButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        jobTitle = findViewById(R.id.jobTitle);
        jobDescription = findViewById(R.id.jobDescription);
        jobLocation = findViewById(R.id.jobLocation);
        jobDate = findViewById(R.id.jobDate);
        postJobButton = findViewById(R.id.postJobButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJob();
                finish();
            }
        });
    }

    private void postJob() {
        String title = jobTitle.getText().toString().trim();
        String description = jobDescription.getText().toString().trim();
        String location = jobLocation.getText().toString().trim();
        String date = jobDate.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (!addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();

                String jobId = UUID.randomUUID().toString();
                Job job = new Job(jobId, title, description, location, date, user.getUid(), latitude, longitude);

                db.collection("jobs").document(jobId).set(job)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Job posted!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to post job", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Invalid location!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
