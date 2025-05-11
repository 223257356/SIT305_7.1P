package com.example.sit305_91p;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sit305_91p.data.DatabaseHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.Locale;

import android.annotation.SuppressLint;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final String TAG = "CreateAdvertActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    DatabaseHelper databaseHelper;
    RadioGroup rgPostType;
    RadioButton rbLost, rbFound;
    EditText etName, etPhone, etDescription, etDate, etLocation;
    Button btnSave, btnGetCurrentLocation;

    // Location & Places variables
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private Double latitude = null;
    private Double longitude = null;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> startAutocomplete;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        databaseHelper = new DatabaseHelper(this);

        // --- Find Views ---
        rgPostType = findViewById(R.id.rgPostType);
        rbLost = findViewById(R.id.rbLost);
        rbFound = findViewById(R.id.rbFound);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        btnSave = findViewById(R.id.btnSave);
        btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);

        // --- Initialize Location/Places SDKs ---
        // Initialize Places SDK
        if (!Places.isInitialized()) {
            String apiKey = "AIzaSyCTwentYgan_QVSReTH87YLEHOb6QJIpF8"; // Updated API key

            // Check if the apiKey is actually empty or a known placeholder that means "not set"
            // For this project, since you've set your key, we assume it's valid if not empty.
            // The original check was: if (apiKey.equals("YOUR_API_KEY_HERE_ORIGINAL_PLACEHOLDER") || apiKey.isEmpty())
            // We'll simplify this for now, assuming the key you've put is intended to be used.
            if (apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_PLACEHOLDER")) { // Use a generic placeholder if you want to keep a check
                Log.e(TAG, "Places API key is empty or a placeholder. Please set a valid API key in CreateAdvertActivity.java");
                Toast.makeText(this, "Places API key not configured. Location features may not work.", Toast.LENGTH_LONG).show();
                // Not initializing Places, so features that depend on it will likely fail.
            } else {
                Places.initialize(getApplicationContext(), apiKey);
            }
        }

        // It's crucial that Places.isInitialized() is true before calling createClient()
        // If initialization failed above (e.g. empty API key), this will still crash.
        // A more robust solution might involve disabling UI elements or finishing the activity if Places can't be initialized.
        try {
            placesClient = Places.createClient(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to create PlacesClient, likely because Places was not initialized. Check API key and logs.", e);
            Toast.makeText(this, "Error initializing location services. Features may be limited.", Toast.LENGTH_LONG).show();
            // Handle the error gracefully, e.g., disable location-dependent UI
            etLocation.setEnabled(false);
            btnGetCurrentLocation.setEnabled(false);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- Register Activity Result Launchers ---
        registerAutocompleteLauncher();
        registerPermissionLauncher();

        // --- Setup UI Listeners ---
        setupLocationAutocomplete();
        setupGetCurrentLocationButton();

        btnSave.setOnClickListener(v -> saveAdvert());
    }

    private void registerAutocompleteLauncher() {
        startAutocomplete = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent(result.getData());
                        Log.i(TAG, "Place selected: " + place.getName() + ", " + place.getAddress() + ", " + place.getLatLng());

                        etLocation.setText(place.getAddress()); // Or place.getName() + ", " + place.getAddress()
                        if (place.getLatLng() != null) {
                            latitude = place.getLatLng().latitude;
                            longitude = place.getLatLng().longitude;
                        }
                    } else if (result.getResultCode() == com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR) {
                        Status status = com.google.android.libraries.places.widget.Autocomplete.getStatusFromIntent(result.getData());
                        Log.e(TAG, "Autocomplete error: " + status.getStatusMessage());
                        Toast.makeText(this, "Error getting location: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        // User canceled the operation.
                        Log.i(TAG, "Autocomplete cancelled.");
                    }
                });
    }

    private void registerPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean fineLocationGranted = permissions.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = permissions.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        // Precise location access granted.
                        fetchCurrentLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        // Only approximate location access granted.
                        fetchCurrentLocation(); // Still try fetching
                        Toast.makeText(this, "Approximate location granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        // No location access granted.
                        Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupLocationAutocomplete() {
        // Make EditText non-focusable to trigger click listener instead of keyboard
        etLocation.setFocusable(false);
        etLocation.setClickable(true);
        etLocation.setOnClickListener(v -> {
            launchAutocompleteIntent();
        });
    }

    private void launchAutocompleteIntent() {
        // Specify the fields to return (incl. address and lat/lng)
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                // Optionally set initial query, country restrictions, etc.
                // .setInitialQuery("123 Main St")
                 .setCountries(Arrays.asList("AU")) // Example: Restrict to Australia
                .build(this);
        startAutocomplete.launch(intent);
    }

    private void setupGetCurrentLocationButton() {
        btnGetCurrentLocation.setOnClickListener(v -> {
            checkAndRequestLocationPermission();
        });
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            fetchCurrentLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Explain why the permission is needed (Optional)
            Toast.makeText(this, "Location permission is needed to get current location.", Toast.LENGTH_LONG).show();
            // Then request the permission
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION // Request both for better user choice
            });
        } else {
            // Directly request the permission
             requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void setLocationFieldToAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            java.util.List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                etLocation.setText(address);
            } else {
                etLocation.setText(""); // fallback to empty if no address found
            }
        } catch (IOException e) {
            e.printStackTrace();
            etLocation.setText(""); // fallback to empty if geocoding fails
        }
    }

    @SuppressLint("MissingPermission") // Permission is checked before calling
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        setLocationFieldToAddress(latitude, longitude);
                        Toast.makeText(this, "Current location acquired.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Last known location is null. Trying to request location update.");
                        Toast.makeText(this, "Could not get current location. Ensure location is enabled.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error getting current location", e);
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveAdvert() {
        String postType = rbLost.isChecked() ? "Lost" : "Found";
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String locationName = etLocation.getText().toString().trim(); // Use the text from the EditText

        // Basic validation (can be expanded)
        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || locationName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use stored latitude/longitude if available, otherwise use a default (e.g., 0.0)
        double lat = (latitude != null) ? latitude : 0.0;
        double lon = (longitude != null) ? longitude : 0.0;

        // Check if lat/lon are still default, maybe warn user if location wasn't properly selected/fetched
        if (lat == 0.0 && lon == 0.0 && !locationName.equals(String.format("Lat: %.4f, Lon: %.4f", 0.0, 0.0))) {
             Log.w(TAG, "Saving advert with default coordinates (0,0) but location name is set.");
             // Consider if you want to prevent saving or force user to select a valid location
             // For now, we'll proceed with saving.
        }


        // Call addItem and check the result
        long itemId = databaseHelper.addItem(postType, name, phone, description, date, locationName, lat, lon);

        if (itemId != -1) {
            Toast.makeText(this, "Advert saved successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity after successful save
        } else {
            Toast.makeText(this, "Error saving advert. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
