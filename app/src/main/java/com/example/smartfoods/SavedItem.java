package com.example.smartfoods;

import java.io.Serializable;

public class SavedItem implements Serializable {
    private String itemName;
    private String expiryDate;
    private int expiryColor;

    // No-argument constructor required for Firestore
    public SavedItem() {
    }

    public SavedItem(String itemName, String expiryDate, int expiryColor) {
        this.itemName = itemName;
        this.expiryDate = expiryDate;
        this.expiryColor = expiryColor;
    }

    public String getItemName() {
        return itemName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getExpiryColor() {
        return expiryColor;
    }
}