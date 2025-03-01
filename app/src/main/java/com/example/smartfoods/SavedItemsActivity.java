package com.example.smartfoods;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SavedItemsActivity extends AppCompatActivity {

    // Static list to store saved items
    public static List<SavedItem> savedItemsList = new ArrayList<>();
    private static final String PREFS_NAME = "SavedItemsPrefs";
    private static final String SAVED_ITEMS_KEY = "savedItems";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        // Load saved items from SharedPreferences
        loadSavedItems();

        // Get the saved item from the intent (if any)
        SavedItem savedItem = (SavedItem) getIntent().getSerializableExtra("savedItem");
        if (savedItem != null) {
            savedItemsList.add(savedItem); // Add the new item to the list
            saveItems(); // Save the updated list
        }

        // Display all saved items
        displaySavedItems();
    }

    private void loadSavedItems() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(SAVED_ITEMS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<SavedItem>>() {}.getType();
            savedItemsList = new Gson().fromJson(json, type);
        }
    }

    private void saveItems() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(savedItemsList);
        editor.putString(SAVED_ITEMS_KEY, json);
        editor.apply();
    }

    private void displaySavedItems() {
        // Find the LinearLayout by its ID
        LinearLayout savedItemsLayout = findViewById(R.id.savedItemsLayout);
        savedItemsLayout.removeAllViews(); // Clear existing views

        // Add each saved item to the layout
        for (int i = 0; i < savedItemsList.size(); i++) {
            SavedItem item = savedItemsList.get(i);

            // Create a horizontal LinearLayout for each item
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(0, 16, 0, 16);

            // Create TextView for the item name and expiry date
            TextView itemView = new TextView(this);
            itemView.setText(item.getItemName() + " - " + item.getExpiryDate());
            itemView.setTextColor(item.getExpiryColor());
            itemView.setTextSize(18);

            // Create Delete Button
            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            int finalI = i;
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savedItemsList.remove(finalI);
                    saveItems();
                    displaySavedItems(); // Refresh the list
                }
            });

            // Add views to the item layout
            itemLayout.addView(itemView);
            itemLayout.addView(deleteButton);

            // Add the item layout to the main layout
            savedItemsLayout.addView(itemLayout);
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate back to the home page (MainActivity)
        super.onBackPressed();
        Intent intent = new Intent(SavedItemsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}