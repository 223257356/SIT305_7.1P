package com.example.sit305_71p;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sit305_71p.data.DatabaseHelper;

public class ItemDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.example.sit305_71p.EXTRA_ITEM_ID";

    DatabaseHelper databaseHelper;
    TextView tvDetailHeader, tvDetailDate, tvDetailLocation, tvDetailDescription, tvDetailPostType, tvDetailPhone;
    Button btnRemove;
    long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        databaseHelper = new DatabaseHelper(this);

        // Find views
        tvDetailHeader = findViewById(R.id.tvDetailHeader);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPostType = findViewById(R.id.tvDetailPostType);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        btnRemove = findViewById(R.id.btnRemove);

        // Get item ID from intent
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

        if (itemId == -1) {
            // Handle error: No ID passed
            Toast.makeText(this, "Error: Item ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadItemDetails();

        btnRemove.setOnClickListener(v -> removeItem());
    }

    private void loadItemDetails() {
        Cursor cursor = databaseHelper.getItemById(itemId);

        if (cursor != null && cursor.moveToFirst()) {
            // Extract data
            String postType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_POST_TYPE));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_PHONE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_DATE));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_LOCATION));

            // Populate TextViews
            tvDetailHeader.setText(postType + ": " + name); // Use postType and name for header
            tvDetailDate.setText(date);
            tvDetailLocation.setText(location);
            tvDetailDescription.setText(description);
            tvDetailPostType.setText(postType);
            tvDetailPhone.setText(phone);

            cursor.close();
        } else {
            // Handle error: Item not found
            Toast.makeText(this, "Error: Could not load item details.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void removeItem() {
        int rowsDeleted = databaseHelper.deleteItem(itemId);
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Item removed successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity after removal
        } else {
            Toast.makeText(this, "Error removing item.", Toast.LENGTH_SHORT).show();
        }
    }

     @Override
    protected void onDestroy() {
        super.onDestroy();
        // databaseHelper.close(); // Consider closing DB if needed
    }
}
