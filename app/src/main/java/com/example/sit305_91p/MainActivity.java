package com.example.sit305_91p;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnCreateAdvert, btnShowItems, btnShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnCreateAdvert = findViewById(R.id.btnCreateAdvert);
        btnShowItems = findViewById(R.id.btnShowItems);
        btnShowMap = findViewById(R.id.btnShowMap);

        btnCreateAdvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to CreateAdvertActivity
                Intent createAdvertIntent = new Intent(MainActivity.this, CreateAdvertActivity.class);
                startActivity(createAdvertIntent);
            }
        });

        btnShowItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to ShowItemsActivity
                Intent showItemsIntent = new Intent(MainActivity.this, ShowItemsActivity.class);
                startActivity(showItemsIntent);
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to MapActivity (to be created)
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
            }
        });
    }
}
