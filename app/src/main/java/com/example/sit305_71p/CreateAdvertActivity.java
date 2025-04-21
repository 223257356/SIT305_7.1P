package com.example.sit305_71p;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sit305_71p.data.DatabaseHelper;

public class CreateAdvertActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    RadioGroup rgPostType;
    RadioButton rbLost, rbFound;
    EditText etName, etPhone, etDescription, etDate, etLocation;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        databaseHelper = new DatabaseHelper(this);

        rgPostType = findViewById(R.id.rgPostType);
        rbLost = findViewById(R.id.rbLost);
        rbFound = findViewById(R.id.rbFound);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveAdvert());
    }

    private void saveAdvert() {
        String postType = rbLost.isChecked() ? "Lost" : "Found";
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Basic validation (can be expanded)
        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addItem(postType, name, phone, description, date, location);

        if (result != -1) {
            Toast.makeText(this, "Advert saved successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after saving
        } else {
            Toast.makeText(this, "Error saving advert", Toast.LENGTH_SHORT).show();
        }
    }
}
