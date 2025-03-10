package com.example.localjobs;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobDescription, jobLocation, jobDate; // Added jobDate
    private Button saveButton;
    private FirebaseFirestore db;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        // Initialize fields including jobDate
        jobTitle = findViewById(R.id.job_title);
        jobDescription = findViewById(R.id.job_description);
        jobLocation = findViewById(R.id.job_location);
        jobDate = findViewById(R.id.job_date); // Added job_date
        saveButton = findViewById(R.id.save_button);
        db = FirebaseFirestore.getInstance();

        // Get the job ID from intent
        jobId = getIntent().getStringExtra("job_id");

        // Fetch current job details from Firestore and populate fields
        loadJobDetails();

        // Handle save button click
        saveButton.setOnClickListener(v -> saveJobDetails());
    }

    private void loadJobDetails() {
        db.collection("jobs").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String location = documentSnapshot.getString("location");
                        String date = documentSnapshot.getString("date"); // Fetching date

                        jobTitle.setText(title);
                        jobDescription.setText(description);
                        jobLocation.setText(location);
                        jobDate.setText(date); // Displaying date
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditJobActivity.this, "Error loading job", Toast.LENGTH_SHORT).show());
    }

    private void saveJobDetails() {
        String updatedTitle = jobTitle.getText().toString().trim();
        String updatedDescription = jobDescription.getText().toString().trim();
        String updatedLocation = jobLocation.getText().toString().trim();
        String updatedDate = jobDate.getText().toString().trim(); // Get updated date

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty() || updatedLocation.isEmpty() || updatedDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Geocoder to get latitude and longitude from location name
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(updatedLocation, 1);
            if (!addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();

                // Update the job in Firestore with the updated date
                db.collection("jobs").document(jobId)
                        .update("title", updatedTitle, "description", updatedDescription,
                                "location", updatedLocation, "latitude", latitude, "longitude", longitude,
                                "date", updatedDate) // Updating the date
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditJobActivity.this, "Job updated successfully", Toast.LENGTH_SHORT).show();
                            finish();  // Close the activity
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditJobActivity.this, "Error updating job", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Invalid location!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show();
        }
    }
}
