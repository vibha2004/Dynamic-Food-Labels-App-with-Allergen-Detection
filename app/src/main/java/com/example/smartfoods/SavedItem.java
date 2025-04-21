package com.example.smartfoods;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    /**
     * Calculates the number of days left until expiry.
     * Returns:
     *   - A positive number if the item is not expired.
     *   - 0 if it expires today.
     *   - A negative number if it has already expired.
     */
    public int getDaysUntilExpiry() {
        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("Expiry Date: Not Found")) {
            return -999; // Invalid expiry date indicator
        }

        try {
            // Extract just the date part (handles "Expiry Date: 20/08/2025" or "20/08/2025")
            String dateOnly = expiryDate.replaceAll(".*?(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}).*", "$1");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setLenient(false);
            Date date = sdf.parse(dateOnly);

            long diffMillis = date.getTime() - System.currentTimeMillis();
            return (int) (diffMillis / (1000 * 60 * 60 * 24)); // Convert milliseconds to days
        } catch (ParseException e) {
            return -999; // Return -999 for parsing errors
        }
    }
}