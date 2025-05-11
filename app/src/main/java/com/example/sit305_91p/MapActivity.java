package com.example.sit305_91p;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.sit305_91p.data.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        databaseHelper = new DatabaseHelper(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error - SupportMapFragment not found!");
            // Handle error, maybe finish activity
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Optional: Enable My Location layer if permissions are granted
        // Requires permission check similar to CreateAdvertActivity
        // if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        //     mMap.setMyLocationEnabled(true);
        // }

        loadMarkers();
    }

    private void loadMarkers() {
        Cursor cursor = null;
        LatLng firstLocation = null; // To move camera to the first item
        boolean isFirst = true;

        try {
            cursor = databaseHelper.getAllItemsWithLocation();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use getColumnIndexOrThrow for robustness
                    @SuppressLint("Range") double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_LATITUDE));
                    @SuppressLint("Range") double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_LONGITUDE));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_NAME));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_POST_TYPE));
                    @SuppressLint("Range") String locationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_LOCATION));

                    // Skip items with default/invalid coordinates if necessary
                    if (lat == 0.0 && lon == 0.0) {
                        Log.w(TAG, "Skipping item with 0,0 coordinates: " + name);
                        continue; // Skip this marker
                    }

                    LatLng itemLocation = new LatLng(lat, lon);
                    if (isFirst) {
                        firstLocation = itemLocation;
                        isFirst = false;
                    }

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(itemLocation)
                            .title(type + ": " + name)
                            .snippet(locationName); // Show location name in snippet

                    mMap.addMarker(markerOptions);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading markers from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Optionally close database helper if needed, though it's usually managed by context
        }

        // Move camera to the first item found, or a default location (e.g., Melbourne)
        LatLng defaultLocation = new LatLng(-37.8136, 144.9631); // Melbourne
        float defaultZoom = 10.0f;

        if (firstLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, defaultZoom));
        } else {
             Log.i(TAG, "No items with locations found, moving camera to default location.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom));
        }
    }
}
