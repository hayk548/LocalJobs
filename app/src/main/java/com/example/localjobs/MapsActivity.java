package com.example.localjobs;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadJobLocations();
    }

    private void loadJobLocations() {
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");

                    LatLng jobLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(jobLocation).title(title));
                }

                if (!task.getResult().isEmpty()) {
                    LatLng firstJob = new LatLng(task.getResult().iterator().next().getDouble("latitude"),
                            task.getResult().iterator().next().getDouble("longitude"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstJob, 10));
                }
            }
        });
    }
}
