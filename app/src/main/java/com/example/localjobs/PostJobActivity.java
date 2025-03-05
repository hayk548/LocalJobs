package com.example.localjobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class PostJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobDescription, jobLocation, jobSalary;
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
        jobSalary = findViewById(R.id.jobSalary);
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
        String salary = jobSalary.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, title, description, location, salary, user.getUid());

        db.collection("jobs")
                .document(jobId)
                .set(job)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostJobActivity.this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostJobActivity.this, JobsActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostJobActivity.this, "Failed to post job", Toast.LENGTH_SHORT).show();
                });

    }
}
