package com.example.localjobs;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditJobActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, categoryEditText;
    private Button saveButton;
    private FirebaseFirestore db;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue)); // Set status bar color
        }

        db = FirebaseFirestore.getInstance();
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        saveButton = findViewById(R.id.saveButton);

        jobId = getIntent().getStringExtra("jobId");

        loadJobDetails(jobId);

        saveButton.setOnClickListener(v -> saveJobDetails());
    }

    private void loadJobDetails(String jobId) {
        db.collection("jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Job job = documentSnapshot.toObject(Job.class);
                    if (job != null) {
                        titleEditText.setText(job.getTitle());
                        descriptionEditText.setText(job.getDescription());
                        categoryEditText.setText(job.getCategory());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditJobActivity.this, "Error loading job details", Toast.LENGTH_SHORT).show());
    }

    private void saveJobDetails() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String category = categoryEditText.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Job job = new Job();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setTitle(title);
        job.setDescription(description);
        job.setCategory(category);

        db.collection("jobs").document(jobId).set(job)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditJobActivity.this, "Job updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditJobActivity.this, "Error updating job", Toast.LENGTH_SHORT).show());
    }
}