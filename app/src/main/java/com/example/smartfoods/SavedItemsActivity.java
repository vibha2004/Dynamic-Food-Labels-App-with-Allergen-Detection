package com.example.smartfoods;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class SavedItemsActivity extends AppCompatActivity {

    // Static list to store saved items
    public static List<SavedItem> savedItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        // Get the saved item from the intent (if any)
        SavedItem savedItem = (SavedItem) getIntent().getSerializableExtra("savedItem");
        if (savedItem != null) {
            savedItemsList.add(savedItem); // Add the new item to the list
        }

        // Display all saved items
        displaySavedItems();
    }

    private void displaySavedItems() {
        // Find the LinearLayout by its ID
        LinearLayout savedItemsLayout = findViewById(R.id.savedItemsLayout);
        savedItemsLayout.removeAllViews(); // Clear existing views

        // Add each saved item to the layout
        for (SavedItem item : savedItemsList) {
            TextView itemView = new TextView(this);
            itemView.setText(item.getItemName() + " - " + item.getExpiryDate());
            itemView.setTextColor(item.getExpiryColor());
            itemView.setTextSize(18);
            itemView.setPadding(0, 16, 0, 16); // Add some padding
            savedItemsLayout.addView(itemView);
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