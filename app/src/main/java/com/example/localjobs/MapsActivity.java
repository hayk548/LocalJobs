package com.example.localjobs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchLocation;
    private Button okButton;
    private Marker selectedMarker;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        searchLocation = findViewById(R.id.searchLocation);
        okButton = findViewById(R.id.okButton);
        geocoder = new Geocoder(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Search on Enter key press
        searchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchAddress();
                return true;
            }
            return false;
        });

        okButton.setOnClickListener(v -> returnSelectedLocation());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable draggable marker
        mMap.setOnMapClickListener(latLng -> {
            updateMarker(latLng);
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Start with a default location (e.g., user's location or a default)
            LatLng defaultLocation = new LatLng(37.7749, -122.4194); // San Francisco as default
            updateMarker(defaultLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void searchAddress() {
        String location = searchLocation.getText().toString().trim();
        if (location.isEmpty()) {
            Toast.makeText(this, "Enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                updateMarker(latLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarker(LatLng latLng) {
        if (selectedMarker != null) {
            selectedMarker.remove();
        }
        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
        selectedMarker.setPosition(latLng); // Update position if dragged
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void returnSelectedLocation() {
        if (selectedMarker == null) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng selectedLatLng = selectedMarker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(selectedLatLng.latitude, selectedLatLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0); // Full address
                Intent result = new Intent();
                result.putExtra("selected_address", addressLine);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, "Could not get address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error getting address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }
}