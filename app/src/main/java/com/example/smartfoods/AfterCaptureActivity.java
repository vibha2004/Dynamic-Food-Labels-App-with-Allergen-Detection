package com.example.smartfoods;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartfoods.ocr.OcrCaptureActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AfterCaptureActivity extends AppCompatActivity {

    private ArrayList<String> itemList;
    private Button anotherPicture, textToSpeechButton, saveButton;
    private ImageView icon;
    private TextView titleText, expiryLabel, novaCategoryLabel,
            artificialSweetenerLabel, foodAttributesLabel, safetyAdviceLabel, allergensLabel;
    private LinearLayout badIngredientsBox;
    private View healthBar1, healthBar2, healthBar3, healthBar4, healthBar5;

    private TextParser parser = new TextParser();
    private Drawable check, negative;
    private String preferences;
    private TextToSpeech textToSpeech;
    private StringBuilder speechText = new StringBuilder();
    private static final long ONE_DAY_IN_MILLIS = 86400000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_capture);

        if (!initializeViews()) {
            Toast.makeText(this, "Failed to initialize views", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();

        if (itemList == null || itemList.isEmpty()) {
            itemList = new ArrayList<>();
            itemList.add("water, sugar, milk, flour");
            Toast.makeText(this, "Fallback ingredients used", Toast.LENGTH_SHORT).show();
        }

        processFoodAnalysis();
    }

    private boolean initializeViews() {
        try {
            anotherPicture = findViewById(R.id.AnotherPicture);
            saveButton = findViewById(R.id.SaveButton);
            textToSpeechButton = findViewById(R.id.TextToSpeech);
            icon = findViewById(R.id.icon);
            titleText = findViewById(R.id.TitleText);
            badIngredientsBox = findViewById(R.id.BadIngredientsBox);
            artificialSweetenerLabel = findViewById(R.id.artificialSweetenerLabel);
            expiryLabel = findViewById(R.id.ExpiryLabel);
            foodAttributesLabel = findViewById(R.id.foodAttributesLabel);
            safetyAdviceLabel = findViewById(R.id.safetyAdviceLabel);
            allergensLabel = findViewById(R.id.allergensLabel);
            novaCategoryLabel = findViewById(R.id.novaCategoryLabel);

            // Initialize health bars
            healthBar1 = findViewById(R.id.healthBar1);
            healthBar2 = findViewById(R.id.healthBar2);
            healthBar3 = findViewById(R.id.healthBar3);
            healthBar4 = findViewById(R.id.healthBar4);
            healthBar5 = findViewById(R.id.healthBar5);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                preferences = extras.getString("preferences", "0000000000");
                itemList = (ArrayList<String>) extras.getSerializable("ING-LIST");
            } else {
                preferences = "0000000000";
                itemList = new ArrayList<>();
            }

            check = ContextCompat.getDrawable(this, R.drawable.check);
            negative = ContextCompat.getDrawable(this, R.drawable.negative);

            initializeTextToSpeech();
            return true;
        } catch (Exception e) {
            Log.e("AfterCapture", "Error initializing views", e);
            return false;
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(0.9f);
            }
        });
    }

    private void setupListeners() {
        anotherPicture.setOnClickListener(view -> {
            Intent intent = new Intent(AfterCaptureActivity.this, OcrCaptureActivity.class);
            intent.putExtra("preferences", preferences);
            startActivity(intent);
        });

        textToSpeechButton.setOnClickListener(view -> {
            if (textToSpeech != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    textToSpeech.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        saveButton.setOnClickListener(view -> {
            String expiryDate = parser.extractExpiryDate(itemList);
            showSaveDialog(expiryDate != null ? expiryDate : "Not Found");
        });
    }

    private void processFoodAnalysis() {
        try {
            parser.setUserPreferences(preferences);
            speechText.setLength(0);
            speechText.append("Food analysis results. ");

            analyzeFoodAttributes();
            analyzeAllergens();
            analyzeExpiryDate();
            analyzeIngredientSafety();
            analyzeProcessingLevel();
            generateSafetyAdvice();
            updateHealthMeter(); // Update the health meter after all analysis is done
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in food analysis", e);
            Toast.makeText(this, "Error analyzing food", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHealthMeter() {
        int healthScore = calculateHealthScore();

        // Reset all bars to gray first
        healthBar1.setBackgroundColor(Color.LTGRAY);
        healthBar2.setBackgroundColor(Color.LTGRAY);
        healthBar3.setBackgroundColor(Color.LTGRAY);
        healthBar4.setBackgroundColor(Color.LTGRAY);
        healthBar5.setBackgroundColor(Color.LTGRAY);

        // Set active bars based on health score
        if (healthScore >= 1) healthBar1.setBackgroundColor(getHealthColor(healthScore));
        if (healthScore >= 2) healthBar2.setBackgroundColor(getHealthColor(healthScore));
        if (healthScore >= 3) healthBar3.setBackgroundColor(getHealthColor(healthScore));
        if (healthScore >= 4) healthBar4.setBackgroundColor(getHealthColor(healthScore));
        if (healthScore >= 5) healthBar5.setBackgroundColor(getHealthColor(healthScore));
    }

    private int calculateHealthScore() {
        int score = 5; // Start with maximum score

        // Deduct points based on various factors
        String novaStatus = novaCategoryLabel.getText().toString();
        if (novaStatus.contains("NOVA 4")) {
            score -= 2; // Ultra-processed foods get big penalty
        } else if (novaStatus.contains("NOVA 3")) {
            score -= 1; // Processed foods get penalty
        }

        String ingredientsStatus = artificialSweetenerLabel.getText().toString();
        if (ingredientsStatus.contains("harmful")) {
            score -= 2; // Harmful ingredients get big penalty
        } else if (ingredientsStatus.contains("cautious")) {
            score -= 1; // Questionable ingredients get penalty
        } else if (ingredientsStatus.contains("High sugar")) {
            score -= 1; // High sugar gets penalty
        }

        String expiryStatus = expiryLabel.getText().toString();
        if (expiryStatus.contains("Expired")) {
            score = 1; // Expired items get minimum score
        } else if (expiryStatus.contains("Near Expiry")) {
            score -= 1; // Near expiry gets penalty
        }

        // Ensure score is between 1 and 5
        return Math.max(1, Math.min(5, score));
    }

    private int getHealthColor(int healthScore) {
        switch (healthScore) {
            case 1: return Color.RED;
            case 2: return Color.rgb(255, 165, 0); // Orange
            case 3: return Color.YELLOW;
            case 4: return Color.rgb(144, 238, 144); // Light green
            case 5: return Color.GREEN;
            default: return Color.GREEN;
        }
    }

    private void analyzeFoodAttributes() {
        try {
            ArrayList<String> vegetarianIssues = parser.checkVegetarian(itemList);
            ArrayList<String> veganIssues = parser.checkVegan(itemList);

            boolean isVegetarian = vegetarianIssues.isEmpty();
            boolean isVegan = veganIssues.isEmpty();
            boolean isOrganic = checkIfOrganic(itemList);
            boolean containsMeat = checkIfContainsMeat(itemList);
            boolean containsDairy = !parser.checkLactose(itemList).isEmpty();

            StringBuilder attributes = new StringBuilder();

            if (isOrganic) {
                attributes.append("🌱 Certified Organic\n");
                speechText.append("This product is certified organic. ");
            }

            if (isVegan) {
                attributes.append("🥕 Vegan Friendly\n");
                speechText.append("This product is vegan friendly. ");
            } else if (isVegetarian) {
                attributes.append("🥕 Vegetarian Friendly\n");
                speechText.append("This product is vegetarian friendly. ");
            } else if (containsMeat) {
                attributes.append("🍖 Contains Meat\n");
                speechText.append("This product contains meat. ");
            } else if (containsDairy) {
                attributes.append("🥛 Contains Dairy/Eggs\n");
                speechText.append("This product contains dairy or eggs. ");
            }

            foodAttributesLabel.setText(attributes.toString());
        } catch (Exception e) {
            Log.e("FoodAnalysis", "Error analyzing food attributes", e);
            foodAttributesLabel.setText("Error analyzing food attributes");
        }
    }

    private void analyzeAllergens() {
        try {
            ArrayList<ArrayList<String>> allergenItems = parser.checkAllergens(itemList);
            ArrayList<String> glutenItems = parser.checkGluten(itemList);
            ArrayList<String> lactoseItems = parser.checkLactose(itemList);

            StringBuilder allergensBuilder = new StringBuilder();

            if (allergenItems.isEmpty() && glutenItems.isEmpty() && lactoseItems.isEmpty()) {
                allergensBuilder.append("✅ No common allergens detected\n");
                speechText.append("No common allergens detected. ");
            } else {
                allergensBuilder.append("⚠️ Contains:\n");

                if (!allergenItems.isEmpty()) {
                    for (ArrayList<String> allergenGroup : allergenItems) {
                        if (!allergenGroup.isEmpty()) {
                            allergensBuilder.append("• ").append(allergenGroup.get(0)).append("\n");
                            speechText.append("Contains ").append(allergenGroup.get(0)).append(". ");
                        }
                    }
                }

                if (!glutenItems.isEmpty()) {
                    allergensBuilder.append("• Gluten\n");
                    speechText.append("Contains gluten. ");
                }

                if (!lactoseItems.isEmpty()) {
                    allergensBuilder.append("• Lactose/Dairy\n");
                    speechText.append("Contains lactose or dairy. ");
                }
            }

            allergensLabel.setText(allergensBuilder.toString());
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in allergen analysis", e);
            allergensLabel.setText("Error checking allergens");
        }
    }

    private void analyzeExpiryDate() {
        try {
            String expiryDateRaw = parser.extractExpiryDate(itemList);
            Log.i("ExpiryDate", "Extracted Expiry Date: " + expiryDateRaw);

            // Safety check before parsing
            if (expiryDateRaw == null || expiryDateRaw.trim().isEmpty() ||
                    expiryDateRaw.equalsIgnoreCase("not found") ||
                    expiryDateRaw.equalsIgnoreCase("n/a")) {
                expiryLabel.setText("Expiry Date: Not Found");
                expiryLabel.setBackgroundColor(Color.GRAY);
                speechText.append("Expiry date not found. ");
                return;
            }

            // Clean the date string
            String cleanDate = expiryDateRaw.replace("Expiry Date:", "").trim();

            long expiryTime = parseExpiryDate(cleanDate);
            if (expiryTime == -1) {
                expiryLabel.setText("Expiry Date: Invalid Format");
                expiryLabel.setBackgroundColor(Color.GRAY);
                speechText.append("Expiry date format invalid. ");
                return;
            }

            long currentTime = System.currentTimeMillis();
            long difference = expiryTime - currentTime;

            if (difference > 30 * ONE_DAY_IN_MILLIS) {
                expiryLabel.setText("Expiry Date: Fresh (" + cleanDate + ")");
                expiryLabel.setBackgroundColor(Color.GREEN);
                speechText.append("Product is fresh with expiry date of ").append(cleanDate).append(". ");
            } else if (difference > 7 * ONE_DAY_IN_MILLIS) {
                expiryLabel.setText("Expiry Date: Good (" + cleanDate + ")");
                expiryLabel.setBackgroundColor(Color.rgb(144, 238, 144)); // Light green
                speechText.append("Product is still good but will expire soon on ").append(cleanDate).append(". ");
            } else if (difference > 0) {
                expiryLabel.setText("Expiry Date: Near Expiry (" + cleanDate + ")");
                expiryLabel.setBackgroundColor(Color.YELLOW);
                speechText.append("Warning! Product is near expiry on ").append(cleanDate).append(". ");
            } else {
                expiryLabel.setText("Expiry Date: Expired (" + cleanDate + ")");
                expiryLabel.setBackgroundColor(Color.RED);
                speechText.append("Warning! Product expired on ").append(cleanDate).append(". ");
            }
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in expiry analysis", e);
            expiryLabel.setText("Error checking expiry date");
            expiryLabel.setBackgroundColor(Color.GRAY);
        }
    }

    private void analyzeIngredientSafety() {
        try {
            ArrayList<String> harmfulAdditives = parser.checkHarmfulAdditives(itemList);
            ArrayList<String> unhealthyFats = parser.checkUnhealthyFats(itemList);
            ArrayList<String> artificialColors = parser.checkArtificialColors(itemList);
            ArrayList<String> highSodiumItems = parser.checkHighSodium(itemList);
            ArrayList<String> sugaryAddictives = parser.checkSugaryAdditives(itemList);
            ArrayList<String> sweetenerItems = parser.checkArtificialSweeteners(itemList);

            StringBuilder safetyBuilder = new StringBuilder();
            int bgColor = Color.GREEN;

            if (!harmfulAdditives.isEmpty() || !unhealthyFats.isEmpty()) {
                safetyBuilder.append("❌ Contains potentially harmful ingredients:\n");
                if (!harmfulAdditives.isEmpty()) {
                    safetyBuilder.append("• ").append(harmfulAdditives.toString()).append("\n");
                }
                if (!unhealthyFats.isEmpty()) {
                    safetyBuilder.append("• ").append(unhealthyFats.toString()).append("\n");
                }
                bgColor = Color.RED;
                speechText.append("Warning! This product contains potentially harmful ingredients. ");
            } else if (!artificialColors.isEmpty() || !highSodiumItems.isEmpty()) {
                safetyBuilder.append("⚠️ Contains ingredients to be cautious of:\n");
                if (!artificialColors.isEmpty()) {
                    safetyBuilder.append("• ").append(artificialColors.toString()).append("\n");
                }
                if (!highSodiumItems.isEmpty()) {
                    safetyBuilder.append("• ").append(highSodiumItems.toString()).append("\n");
                }
                bgColor = Color.rgb(255, 165, 0);
                speechText.append("This product contains some ingredients that should be consumed in moderation. ");
            } else if (!sugaryAddictives.isEmpty()) {
                safetyBuilder.append("⚠️ High sugar content:\n")
                        .append(sugaryAddictives.toString()).append("\n");
                bgColor = Color.YELLOW;
                speechText.append("This product contains high amounts of sugar. ");
            } else if (!sweetenerItems.isEmpty()) {
                safetyBuilder.append("⚠️ Contains artificial sweeteners:\n")
                        .append(sweetenerItems.toString()).append("\n")
                        .append("✅ Other ingredients are safe\n");
                bgColor = Color.GREEN;
                speechText.append("This product contains artificial sweeteners but other ingredients are safe. ");
            } else {
                safetyBuilder.append("✅ All ingredients are safe\n");
                speechText.append("All ingredients in this product are safe. ");
            }

            artificialSweetenerLabel.setText(safetyBuilder.toString());
            artificialSweetenerLabel.setBackgroundColor(bgColor);
            artificialSweetenerLabel.setTextColor(Color.BLACK);
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in ingredient safety", e);
            artificialSweetenerLabel.setText("Error analyzing ingredients");
        }
    }

    private void analyzeProcessingLevel() {
        try {
            String novaCategory = parser.determineNovaCategory(itemList);
            displayNovaClassification(novaCategory);
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in processing level", e);
            novaCategoryLabel.setText("Error determining processing level");
        }
    }

    private void displayNovaClassification(String novaCategory) {
        int novaColor;
        String novaText;

        if (novaCategory == null) {
            novaCategory = "";
        }

        if (novaCategory.contains("NOVA 4")) {
            novaColor = Color.RED;
            novaText = "🚨 Ultra-Processed (NOVA 4)\n" +
                    "• Highly processed with many additives\n" +
                    "• Linked to health risks with frequent consumption";
            speechText.append("This is an ultra-processed food that should be avoided. ");
        } else if (novaCategory.contains("NOVA 3")) {
            novaColor = Color.rgb(255, 165, 0);
            novaText = "⚠️ Processed (NOVA 3)\n" +
                    "• Contains some processed ingredients\n" +
                    "• Moderate consumption recommended";
            speechText.append("This is a processed food that should be eaten in moderation. ");
        } else if (novaCategory.contains("NOVA 2")) {
            novaColor = Color.rgb(255, 255, 0);
            novaText = "🟡 Processed Ingredients (NOVA 2)\n" +
                    "• Contains basic processed ingredients\n" +
                    "• Generally safe for regular consumption";
            speechText.append("This food contains some processed ingredients but is generally safe. ");
        } else {
            novaColor = Color.GREEN;
            novaText = "✅ Unprocessed/Minimally Processed (NOVA 1)\n" +
                    "• Whole, natural ingredients\n" +
                    "• Healthiest option for regular consumption";
            speechText.append("This is a minimally processed healthy food. ");
        }

        novaCategoryLabel.setText(novaText);
        novaCategoryLabel.setBackgroundColor(novaColor);
        novaCategoryLabel.setTextColor(Color.BLACK);
    }

    private void generateSafetyAdvice() {
        try {
            StringBuilder adviceBuilder = new StringBuilder();

            String expiryStatus = expiryLabel.getText().toString();
            String novaStatus = novaCategoryLabel.getText().toString();
            String ingredientsStatus = artificialSweetenerLabel.getText().toString();

            if (expiryStatus.contains("Expired")) {
                adviceBuilder.append("❌ Do not consume - product is expired\n");
            } else if (expiryStatus.contains("Near Expiry")) {
                adviceBuilder.append("⚠️ Consume immediately - product is near expiry\n");
            }

            if (novaStatus.contains("NOVA 4")) {
                adviceBuilder.append("❌ Avoid frequent consumption - ultra-processed food\n");
            } else if (novaStatus.contains("NOVA 3")) {
                adviceBuilder.append("⚠️ Limit consumption - processed food\n");
            }

            if (ingredientsStatus.contains("harmful")) {
                adviceBuilder.append("❌ Avoid - contains harmful ingredients\n");
            } else if (ingredientsStatus.contains("cautious")) {
                adviceBuilder.append("⚠️ Consume in moderation - contains questionable ingredients\n");
            } else if (ingredientsStatus.contains("High sugar")) {
                adviceBuilder.append("⚠️ Limit consumption - high sugar content\n");
            } else {
                adviceBuilder.append("✅ Safe for regular consumption\n");
            }

            adviceBuilder.append("\nRecommendation: ");
            if (adviceBuilder.toString().contains("❌")) {
                adviceBuilder.append("Avoid this product");
            } else if (adviceBuilder.toString().contains("⚠️")) {
                adviceBuilder.append("Consume in moderation");
            } else {
                adviceBuilder.append("Safe for regular consumption");
            }

            safetyAdviceLabel.setText(adviceBuilder.toString());
        } catch (Exception e) {
            Log.e("AfterCapture", "Error generating safety advice", e);
            safetyAdviceLabel.setText("Error generating safety advice");
        }
    }


    private boolean checkIfOrganic(ArrayList<String> ingredients) {
        if (ingredients == null) return false;

        for (String ingredient : ingredients) {
            if (ingredient != null && ingredient.toLowerCase().contains("organic")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfContainsMeat(ArrayList<String> ingredients) {
        if (ingredients == null) return false;

        String[] meatKeywords = {"meat", "beef", "pork", "chicken", "lamb", "fish", "seafood", "bacon", "ham"};
        for (String ingredient : ingredients) {
            if (ingredient != null) {
                for (String keyword : meatKeywords) {
                    if (ingredient.toLowerCase().contains(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long parseExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return -1;
        }

        try {
            // Try multiple date formats
            SimpleDateFormat[] formats = {
                    new SimpleDateFormat("dd/MM/yyyy", Locale.US),
                    new SimpleDateFormat("dd-MM-yyyy", Locale.US),
                    new SimpleDateFormat("MM/dd/yyyy", Locale.US),
                    new SimpleDateFormat("MM-dd-yyyy", Locale.US),
                    new SimpleDateFormat("yyyy/MM/dd", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US)
            };

            for (SimpleDateFormat sdf : formats) {
                try {
                    Date date = sdf.parse(expiryDate);
                    if (date != null) {
                        return date.getTime();
                    }
                } catch (ParseException e) {
                    // Try next format
                }
            }
        } catch (Exception e) {
            Log.e("ExpiryDate", "Error parsing date: " + expiryDate, e);
        }

        return -1;
    }

    private void showSaveDialog(String expiryDate) {
        try {
            final EditText input = new EditText(this);
            input.setHint("Enter item name");

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Save Item")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String itemName = input.getText().toString();
                        if (!itemName.isEmpty()) {
                            saveItem(itemName, expiryDate);
                        } else {
                            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        } catch (Exception e) {
            Log.e("AfterCapture", "Error in save dialog", e);
            Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveItem(String itemName, String expiryDate) {
        try {
            int expiryColor = getExpiryColor(expiryDate);
            SavedItem savedItem = new SavedItem(itemName, expiryDate, expiryColor);

            Intent intent = new Intent(this, SavedItemsActivity.class);
            intent.putExtra("savedItem", savedItem);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("AfterCapture", "Error saving item", e);
            Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
        }
    }

    private int getExpiryColor(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equalsIgnoreCase("Not Found")) {
            return Color.GRAY;
        }

        long expiryTime = parseExpiryDate(expiryDate);
        if (expiryTime == -1) {
            return Color.GRAY;
        }

        long difference = expiryTime - System.currentTimeMillis();

        if (difference > 30 * ONE_DAY_IN_MILLIS) {
            return Color.GREEN;
        } else if (difference > 7 * ONE_DAY_IN_MILLIS) {
            return Color.rgb(144, 238, 144);
        } else if (difference > 0) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("preferences", preferences);
        startActivity(i);
        finish();
    }
}