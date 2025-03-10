package com.example.localjobs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JobsActivity extends AppCompatActivity {

    private RecyclerView jobsRecyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private Button postJobButton, accountButton, btnSortJobs;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0.0, userLng = 0.0;
    private boolean sortByNearest = true;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        postJobButton = findViewById(R.id.postJobButton);
        accountButton = findViewById(R.id.accountButton);
        btnSortJobs = findViewById(R.id.btnSortJobs);
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);

        db = FirebaseFirestore.getInstance();
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, this);

        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsRecyclerView.setAdapter(jobAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        postJobButton.setOnClickListener(v -> startActivity(new Intent(JobsActivity.this, PostJobActivity.class)));
        accountButton.setOnClickListener(v -> startActivity(new Intent(JobsActivity.this, AccountActivity.class)));

        btnSortJobs.setOnClickListener(v -> {
            sortByNearest = !sortByNearest;
            btnSortJobs.setText(sortByNearest ? "Nearest First" : "Farthest First");
            loadJobs();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                loadJobs(); // Load jobs after getting location
            }
        });
    }

    private void loadJobs() {
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    jobList.add(job);
                }

                // Sort jobs by distance
                jobList.sort((job1, job2) -> {
                    double distance1 = calculateDistance(userLat, userLng, job1.getLatitude(), job1.getLongitude());
                    double distance2 = calculateDistance(userLat, userLng, job2.getLatitude(), job2.getLongitude());
                    return sortByNearest ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                });

                jobAdapter.notifyDataSetChanged();
            }
        });
    }

    private double calculateDistance(double userLat, double userLng, double jobLat, double jobLng) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(jobLat - userLat);
        double lonDistance = Math.toRadians(jobLng - userLng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(jobLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }
}
