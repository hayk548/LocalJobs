package com.example.localjobs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditJobActivity extends AppCompatActivity {

    private static final String TAG = "EditJobActivity";
    private EditText jobTitle, jobDescription, jobLocation, jobDate;
    private Spinner jobCategory;
    private Button saveJobButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String jobId;
    private ActivityResultLauncher<Intent> mapLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        jobTitle = findViewById(R.id.jobTitle);
        jobDescription = findViewById(R.id.jobDescription);
        jobLocation = findViewById(R.id.jobLocation);
        jobDate = findViewById(R.id.jobDate);
        jobCategory = findViewById(R.id.job_category);
        saveJobButton = findViewById(R.id.saveJobButton);

        jobId = getIntent().getStringExtra("jobId");

        // Set up category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.job_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobCategory.setAdapter(adapter);

        // Register for activity result from MapsActivity
        mapLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String selectedAddress = result.getData().getStringExtra("selected_address");
                if (selectedAddress != null) {
                    jobLocation.setText(selectedAddress);
                }
            }
        });

        // Make jobLocation clickable to open MapsActivity
        jobLocation.setOnClickListener(v -> {
            Intent intent = new Intent(EditJobActivity.this, MapsActivity.class);
            mapLauncher.launch(intent);
        });

        // Make jobDate clickable to open DatePickerDialog
        jobDate.setOnClickListener(v -> showDatePickerDialog());

        // Load job details
        loadJobDetails(jobId);

        saveJobButton.setOnClickListener(v -> saveJobDetails());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    jobDate.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadJobDetails(String jobId) {
        db.collection("jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Job job = documentSnapshot.toObject(Job.class);
                    if (job != null) {
                        jobTitle.setText(job.getTitle());
                        jobDescription.setText(job.getDescription());
                        jobLocation.setText(job.getLocation());
                        jobDate.setText(job.getDate());
                        // Set spinner to the job's category
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) jobCategory.getAdapter();
                        int position = adapter.getPosition(job.getCategory());
                        if (position >= 0) {
                            jobCategory.setSelection(position);
                        }
                    } else {
                        Toast.makeText(EditJobActivity.this, "Job not found", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Job document not found for jobId: " + jobId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditJobActivity.this, "Error loading job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load job: " + e.getMessage(), e);
                });
    }

    private void saveJobDetails() {
        String title = jobTitle.getText().toString().trim();
        String description = jobDescription.getText().toString().trim();
        String location = jobLocation.getText().toString().trim();
        String date = jobDate.getText().toString().trim();
        String category = jobCategory.getSelectedItem().toString();
        String userId = mAuth.getCurrentUser().getUid();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || date.isEmpty() || category.equals("All")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        double latitude = 0, longitude = 0;
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            Toast.makeText(this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Geocoding error: " + e.getMessage(), e);
            return;
        }

        Job job = new Job(jobId, title, description, location, date, userId, latitude, longitude, category);

        db.collection("jobs").document(jobId).set(job)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditJobActivity.this, "Job updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditJobActivity.this, "Error updating job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update job: " + e.getMessage(), e);
                });
    }
}