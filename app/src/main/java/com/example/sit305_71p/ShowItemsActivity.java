package com.example.sit305_71p;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
// import android.widget.Toast; // No longer needed for placeholder

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sit305_71p.adapter.ItemAdapter;
import com.example.sit305_71p.data.DatabaseHelper;

public class ShowItemsActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

    RecyclerView recyclerViewItems;
    DatabaseHelper databaseHelper;
    ItemAdapter itemAdapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_items);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        databaseHelper = new DatabaseHelper(this);

        setupRecyclerView();
        // loadItems() is called in onResume initially
    }

    private void setupRecyclerView() {
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with null cursor initially, listener is this activity
        itemAdapter = new ItemAdapter(this, null, this);
        recyclerViewItems.setAdapter(itemAdapter);
    }

    private void loadItems() {
        // Ensure previous cursor is closed if exists
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = databaseHelper.getAllItems();
        itemAdapter.swapCursor(cursor); // Update the adapter with the new cursor
    }

    @Override
    public void onItemClick(long id) {
        // Handle item click - Navigate to ItemDetailsActivity
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra(ItemDetailsActivity.EXTRA_ITEM_ID, id); // Pass the item ID
        startActivity(intent);
       // Toast.makeText(this, "Clicked item ID: " + id, Toast.LENGTH_SHORT).show(); // Placeholder removed
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems(); // Refresh list when activity resumes (e.g., after deleting an item)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close(); // Close cursor to prevent memory leaks
        }
        // databaseHelper.close(); // Close database connection if helper manages it exclusively here
    }
}
