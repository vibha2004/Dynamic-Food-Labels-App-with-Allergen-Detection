package com.example.smartfoods;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
            saveItemsToPreferences();
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
        private final TextView daysLeftText; // TextView for days remaining
        private final Button deleteButton;

        public SavedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            expiryDate = itemView.findViewById(R.id.expiryDate);
            daysLeftText = itemView.findViewById(R.id.daysLeftText); // Add new TextView
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(SavedItem item) {
            itemName.setText(item.getItemName());
            expiryDate.setText("Expiry Date: " + item.getExpiryDate());

            // Show days remaining
            int daysLeft = item.getDaysUntilExpiry();
            if (daysLeft >= 0) {
                daysLeftText.setText(daysLeft + " days left");
            } else {
                daysLeftText.setText("Expired");
            }

            setExpiryColor(item.getExpiryDate());
        }

        public void setExpiryColor(String expiryText) {
            int newColor = getExpiryColor(expiryText);
            expiryDate.setTextColor(newColor);
            daysLeftText.setTextColor(newColor); // Also update days left text color
        }

        private int getExpiryColor(String expiryText) {
            if (expiryText == null || expiryText.isEmpty()) {
                return Color.GRAY;
            }

            try {
                String dateOnly = expiryText.replaceAll(".*?(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}).*", "$1");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                sdf.setLenient(false);
                Date date = sdf.parse(dateOnly);

                Calendar expiryCal = Calendar.getInstance();
                expiryCal.setTime(date);

                Calendar now = Calendar.getInstance();
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



