package com.example.smartfoods;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
    private static final String CHANNEL_ID = "food_expiry_alerts";

    public static List<SavedItem> savedItemsList = new ArrayList<>();
    private SavedItemAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        loadSavedItems();
        //sendTestNotification("Food Expiry Status" , "this is working");

        SavedItem savedItem = (SavedItem) getIntent().getSerializableExtra("savedItem");
        if (savedItem != null) {
            savedItemsList.add(savedItem);
            saveItems();
        }

        adapter = new SavedItemAdapter(this, savedItemsList);
        recyclerView.setAdapter(adapter);

        notifyItemsWithExpiryColor();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedItems();
        notifyItemsWithExpiryColor();
    }

    private void loadSavedItems() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(SAVED_ITEMS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<SavedItem>>() {}.getType();
            savedItemsList = new Gson().fromJson(json, type);
        }
    }

    private void refreshSavedItems() {
        loadSavedItems();
        adapter.notifyDataSetChanged();
    }

    private void saveItems() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(savedItemsList);
        editor.putString(SAVED_ITEMS_KEY, json);
        editor.apply();
    }

    private void notifyItemsWithExpiryColor() {
        StringBuilder expiredText = new StringBuilder();
        StringBuilder warningText = new StringBuilder();
        StringBuilder safeText = new StringBuilder();

        for (SavedItem item : savedItemsList) {
            int expiryColor = item.getExpiryColor();
            String itemName = item.getItemName();

            if (expiryColor == -65536) { // Red
                expiredText.append("• ").append(itemName).append(" is expired.\n");
            } else if (expiryColor == -16711936) { // Green
                safeText.append("• ").append(itemName).append(" is safe.\n");
            } else { // Yellow or others
                warningText.append("• ").append(itemName).append(" is about to expire.\n");
            }
        }

        StringBuilder finalNotificationText = new StringBuilder();

        if (expiredText.length() > 0) {
            finalNotificationText.append("❌ Expired Items:\n").append(expiredText).append("\n");
        }
        if (warningText.length() > 0) {
            finalNotificationText.append("⚠ Near Expiry:\n").append(warningText).append("\n");
        }
        if (safeText.length() > 0) {
            finalNotificationText.append("✅ Safe Items:\n").append(safeText);
        }

        if (finalNotificationText.length() > 0) {
            sendGroupedNotification("Food Expiry Summary", "Tap to view details", finalNotificationText.toString());
        }
    }

    private void sendGroupedNotification(String title, String summary, String expandedText) {
        createNotificationChannel();

        Intent intent = new Intent(this, SavedItemsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_MUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(expandedText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2001, builder.build());
            Log.d("NotificationTest", "Grouped notification sent");
        } else {
            Log.e("NotificationTest", "NotificationManager is null");
        }
    }

    private String getColorLabel(int color) {
        if (color == -65536) {
            return "Expired";
        } else if (color == -16711936) {
            return "Safe";
        }else {
            return "About to Expire";
        }
    }




    private void sendTestNotification(String title, String message) {
        createNotificationChannel();

        Intent intent = new Intent(this, SavedItemsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_MUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Using system icon for test
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
            Log.d("NotificationTest", "Test notification sent");
        } else {
            Log.e("NotificationTest", "NotificationManager is null");
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Food Expiry Alerts";
            String description = "Notifications for food items expiry status";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SavedItemsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}