package com.example.localjobs;

import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private double userLat, userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        double userLat = intent.getDoubleExtra("user_lat", 0.0);
        double userLng = intent.getDoubleExtra("user_lng", 0.0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();

                    LatLng userLatLng = new LatLng(userLat, userLng);
                    mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));

                    loadJobLocations();
                }
            }
        });
    }

    private void loadJobLocations() {
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Job> jobList = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    if (job != null) {
                        LatLng jobLocation = new LatLng(job.getLatitude(), job.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(jobLocation).title(job.getTitle()));

                        double distance = calculateDistance(userLat, userLng, job.getLatitude(), job.getLongitude());
                        jobList.add(new Job(job.getJobId(), job.getTitle(), job.getDescription(), job.getLocation(), job.getDate(), job.getUserId(), job.getLatitude(), job.getLongitude(), job.getCategory()));
                    }
                }

                Collections.sort(jobList, Comparator.comparingDouble(job -> calculateDistance(userLat, userLng, job.getLatitude(), job.getLongitude())));

                if (!jobList.isEmpty()) {
                    LatLng nearestJob = new LatLng(jobList.get(0).getLatitude(), jobList.get(0).getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestJob, 10));
                }
            }
        });
    }

    private double calculateDistance(double userLat, double userLng, double jobLat, double jobLng) {
        final int R = 6371;
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
