package com.example.smartfoods;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfoods.ocr.OcrCaptureActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AfterCaptureActivity extends AppCompatActivity {

    ArrayList<String> itemList;
    Button anotherPicture;
    Button textToSpeechButton;
    Button saveButton;
    ImageView icon;
    TextView titleText;
    TextView expiryLabel;
    TextParser parser = new TextParser();
    LinearLayout badIngredientsBox;
    Drawable check;
    Drawable negative;
    String preferences;
    TextToSpeech ts;
    StringBuilder speechText = new StringBuilder();
    private static final long ONE_DAY_IN_MILLIS = 86400000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_capture);

        anotherPicture = findViewById(R.id.AnotherPicture);
        saveButton = findViewById(R.id.SaveButton);
        preferences = getIntent().getExtras().getString("preferences");
        Log.i("Prefs:", "In the after capture act " + preferences);

        itemList = (ArrayList<String>) getIntent().getSerializableExtra("ING-LIST");
        icon = findViewById(R.id.icon);
        titleText = findViewById(R.id.TitleText);
        badIngredientsBox = findViewById(R.id.BadIngredientsBox);
        textToSpeechButton = findViewById(R.id.TextToSpeech);
        expiryLabel = findViewById(R.id.ExpiryLabel);

        Log.i("ItemList", "Item List: " + itemList.toString());

        parser.setUserPreferences(preferences);

        check = getResources().getDrawable(R.drawable.check);
        negative = getResources().getDrawable(R.drawable.negative);

        ts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ts.setLanguage(Locale.US);
                }
            }
        });

        ts.setSpeechRate(0.9f);

        for (int i = 0; i < itemList.size(); i++) {
            Log.i("ITEM " + i, itemList.get(i));
        }

        ArrayList<ArrayList<String>> allergenItems = parser.checkAllergens(itemList);
        ArrayList<String> lactoseItems = parser.checkLactose(itemList);
        ArrayList<String> veganItems = parser.checkVegan(itemList);
        ArrayList<String> vegetarianItems = parser.checkVegaterian(itemList);
        ArrayList<String> glutenItems = parser.checkGluten(itemList);

        if (noBadIngredients(allergenItems, lactoseItems, veganItems, vegetarianItems, glutenItems)) {
            speechText.append("The ingredients are okay.");
            icon.setImageDrawable(check);
        } else {
            speechText.append("The ingredients are not okay, ");
            icon.setImageDrawable(check);
            titleText.setText("Ingredients are not OK. ");
            titleText.setTextColor(Color.rgb(209, 89, 98));
            icon.setImageDrawable(negative);

            if (allergenItems.size() > 0) {
                displayNegativeNested(allergenItems);
            }
            if (lactoseItems.size() > 0) {
                displayNegative(lactoseItems);
            }
            if (veganItems.size() > 0) {
                displayNegative(veganItems);
            }
            if (vegetarianItems.size() > 0) {
                displayNegative(vegetarianItems);
            }
            if (glutenItems.size() > 0) {
                displayNegative(glutenItems);
            }
        }

        String expiryDate = parser.extractExpiryDate(itemList);
        Log.i("ExpiryDate", "Extracted Expiry Date: " + expiryDate);
        checkExpiryDate(expiryDate);

        anotherPicture.setOnClickListener(view -> {
            Intent intent = new Intent(AfterCaptureActivity.this, OcrCaptureActivity.class);
            intent.putExtra("preferences", preferences);
            startActivity(intent);
        });

        textToSpeechButton.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ts.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                ts.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        saveButton.setOnClickListener(view -> showSaveDialog(expiryDate));
    }

    private void showSaveDialog(String expiryDate) {
        final EditText input = new EditText(this);
        input.setHint("Enter item name");

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Save Item")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String itemName = input.getText().toString();
                    if (!itemName.isEmpty()) {
                        String expiryLabelText = (expiryDate == null || expiryDate.isEmpty()) ?
                                "Expiry Date: Not Found" : expiryDate;
                        saveItem(itemName, expiryLabelText);
                    } else {
                        Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void saveItem(String itemName, String expiryDate) {
        int expiryColor = getExpiryColor(expiryDate);
        SavedItem savedItem = new SavedItem(itemName, expiryDate, expiryColor);

        Intent intent = new Intent(this, SavedItemsActivity.class);
        intent.putExtra("savedItem", savedItem);
        startActivity(intent);
        finish();
    }

    private int getExpiryColor(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("Expiry Date: Not Found")) {
            return Color.GRAY;
        }

        long expiryTime = parseExpiryDate(expiryDate);
        if (expiryTime == -1) {
            return Color.GRAY;
        }

        long difference = expiryTime - System.currentTimeMillis();

        if (difference > 7 * ONE_DAY_IN_MILLIS) {
            return Color.GREEN;
        } else if (difference > 0) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private long parseExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return -1;
        }

        try {
            // Extract just the date part (handles "Expiry Date: 20/08/2025" or "20/08/2025")
            String dateOnly = expiryDate.replaceAll(".*?(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}).*", "$1");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setLenient(false);
            Date date = sdf.parse(dateOnly);
            return date.getTime();
        } catch (ParseException e) {
            Log.e("ExpiryDate", "Error parsing expiry date: " + expiryDate, e);
            return -1;
        }
    }

    private void checkExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("Expiry Date: Not Found")) {
            expiryLabel.setText("Expiry Date: Not Found");
            expiryLabel.setBackgroundColor(Color.GRAY);
            return;
        }

        long expiryTime = parseExpiryDate(expiryDate);
        if (expiryTime == -1) {
            expiryLabel.setText("Expiry Date: Invalid Format");
            expiryLabel.setBackgroundColor(Color.GRAY);
            Log.e("ExpiryDate", "Invalid expiry date format: " + expiryDate);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long difference = expiryTime - currentTime;

        if (difference > 7 * ONE_DAY_IN_MILLIS) {
            expiryLabel.setText("Expiry Date: Fresh (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.GREEN);
        } else if (difference > 0) {
            expiryLabel.setText("Expiry Date: Near Expiry (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.YELLOW);
        } else {
            expiryLabel.setText("Expiry Date: Expired (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.RED);
        }
    }

    private boolean noBadIngredients(ArrayList<ArrayList<String>> a,
                                     ArrayList<String> b,
                                     ArrayList<String> c,
                                     ArrayList<String> d,
                                     ArrayList<String> e) {
        return (a.size() == 0) && (b.size() == 0) && (c.size() == 0) && (d.size() == 0) && (e.size() == 0);
    }

    private void displayNegativeNested(ArrayList<ArrayList<String>> result) {
        for (int i = 0; i < result.size() - 1; i++) {
            for (int j = 0; j < result.get(i).size(); j++) {
                TextView text = new TextView(this);
                text.setText(result.get(i).get(j));
                text.setTextColor(Color.rgb(209, 89, 98));
                text.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                badIngredientsBox.addView(text);
            }
        }
        speechText.append(result.get(result.size() - 1)).append(" ");
    }

    private void displayNegative(ArrayList<String> result) {
        for (int i = 0; i < result.size() - 1; i++) {
            TextView text = new TextView(this);
            text.setText(result.get(i));
            text.setTextColor(Color.rgb(209, 89, 98));
            text.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            badIngredientsBox.addView(text);
        }
        speechText.append(result.get(result.size() - 1)).append(" ");
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