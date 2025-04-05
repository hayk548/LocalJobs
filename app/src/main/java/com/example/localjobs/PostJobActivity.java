package com.example.localjobs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class PostJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobDescription, jobLocation, jobDate;
    private Button postJobButton;
    private Spinner jobCategorySpinner;
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
        jobCategorySpinner = findViewById(R.id.job_category);
        postJobButton = findViewById(R.id.postJobButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set date picker on jobDate EditText
        jobDate.setOnClickListener(v -> showDatePickerDialog());

        postJobButton.setOnClickListener(v -> {
            postJob();
            finish();
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.job_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobCategorySpinner.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Adjust month (DatePickerDialog returns months 0-11)
                    selectedMonth++;
                    String formattedDate = String.format(Locale.US, "%02d/%02d/%d", selectedMonth, selectedDay, selectedYear);
                    jobDate.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void postJob() {
        String title = jobTitle.getText().toString().trim();
        String description = jobDescription.getText().toString().trim();
        String location = jobLocation.getText().toString().trim();
        String date = jobDate.getText().toString().trim();
        String category = jobCategorySpinner.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || date.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Continue with geocoding and Firebase job posting logic...
    }
}
