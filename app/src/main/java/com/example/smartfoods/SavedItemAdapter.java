package com.example.smartfoods;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedItemAdapter extends RecyclerView.Adapter<SavedItemAdapter.SavedItemViewHolder> {

    private List<SavedItem> savedItems;

    public SavedItemAdapter(List<SavedItem> savedItems) {
        this.savedItems = savedItems;
    }

    @NonNull
    @Override
    public SavedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved, parent, false);
        return new SavedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedItemViewHolder holder, int position) {
        SavedItem item = savedItems.get(position);
        holder.itemName.setText(item.getItemName());
        holder.expiryDate.setText(item.getExpiryDate());
        holder.expiryDate.setTextColor(item.getExpiryColor());
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    public static class SavedItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView expiryDate;

        public SavedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            expiryDate = itemView.findViewById(R.id.expiryDate);
        }
    }
}