package com.example.smartfoods;
import java.util.Calendar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import java.util.List;

public class SavedItemAdapter extends RecyclerView.Adapter<SavedItemAdapter.SavedItemViewHolder> {

    private final List<SavedItem> savedItems;
    private final Context context;

    public SavedItemAdapter(Context context, List<SavedItem> savedItems) {
        this.context = context;
        this.savedItems = savedItems;
    }

    @NonNull
    @Override
    public SavedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved, parent, false);
        return new SavedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedItemViewHolder holder, int position) {
        SavedItem item = savedItems.get(position);
        holder.bind(item);

        // Ensure expiry color updates dynamically
        holder.expiryDate.post(() -> holder.setExpiryColor(item.getExpiryDate()));

        // Handle item deletion
        holder.deleteButton.setOnClickListener(view -> {
            savedItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, savedItems.size());
            saveItemsToPreferences(); // Save updated list
        });
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    // Save updated list after deletion
    private void saveItemsToPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SavedItemsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(savedItems);
        editor.putString("savedItems", json);
        editor.apply();
    }

    static class SavedItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final TextView expiryDate;
        private final Button deleteButton; // Add delete button

        public SavedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            expiryDate = itemView.findViewById(R.id.expiryDate);
            deleteButton = itemView.findViewById(R.id.deleteButton); // Initialize delete button
        }

        public void bind(SavedItem item) {
            itemName.setText(item.getItemName());
            expiryDate.setText(item.getExpiryDate());
            setExpiryColor(item.getExpiryDate());
        }

        public void setExpiryColor(String expiryText) {
            int newColor = getExpiryColor(expiryText);
            expiryDate.setTextColor(newColor);
            expiryDate.invalidate(); // Force UI redraw
        }

        private int getExpiryColor(String expiryText) {
            if (expiryText == null || expiryText.isEmpty()) {
                return Color.GRAY;
            }

            try {
                String numbersOnly = expiryText.replaceAll("[^0-9/-]", "");
                String[] parts = numbersOnly.split("[/-]");

                if (parts.length != 3) {
                    return Color.GRAY;
                }

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                if (parts[2].length() == 2) {
                    year += 2000;
                }

                Calendar expiryCal = Calendar.getInstance();
                expiryCal.set(year, month - 1, day, 0, 0, 0);
                expiryCal.set(Calendar.MILLISECOND, 0);

                Calendar now = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);

                long diffDays = (expiryCal.getTimeInMillis() - now.getTimeInMillis()) / (24 * 60 * 60 * 1000);

                if (diffDays > 7) {
                    return Color.GREEN;
                } else if (diffDays >= 0) {
                    return Color.YELLOW;
                } else {
                    return Color.RED;
                }

            } catch (Exception e) {
                Log.e("DATE_ERROR", "Error processing date: " + expiryText, e);
                return Color.GRAY;
            }
        }
    }
}


