package com.example.smartfoods;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SavedItemsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SavedItemsPrefs";
    private static final String SAVED_ITEMS_KEY = "savedItems";
    public static List<SavedItem> savedItemsList = new ArrayList<>();
    private SavedItemAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSavedItems();

        SavedItem savedItem = (SavedItem) getIntent().getSerializableExtra("savedItem");
        if (savedItem != null) {
            savedItemsList.add(savedItem);
            saveItems();
        }

        adapter = new SavedItemAdapter(this, savedItemsList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedItems(); // Refresh list when activity is reopened
    }

    private void refreshSavedItems() {
        loadSavedItems();
        adapter.notifyDataSetChanged(); // Force UI update to recalculate expiry colors
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SavedItemsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
