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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    ImageView icon;
    TextView titleText;
    TextView expiryLabel; // Add this field
    TextParser parser = new TextParser();
    LinearLayout badIngredientsBox;
    Drawable check;
    Drawable negative;
    String preferences;
    TextToSpeech ts;
    StringBuilder speechText = new StringBuilder();
    private static final long ONE_DAY_IN_MILLIS = 86400000; // 24 hours in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_capture);

        anotherPicture = findViewById(R.id.AnotherPicture);
        preferences = getIntent().getExtras().getString("preferences");
        Log.i("Prefs:", "In the after capture act " + preferences);

        itemList = (ArrayList<String>) getIntent().getSerializableExtra("ING-LIST");
        icon = findViewById(R.id.icon);
        titleText = findViewById(R.id.TitleText);
        badIngredientsBox = findViewById(R.id.BadIngredientsBox);
        textToSpeechButton = findViewById(R.id.TextToSpeech);
        expiryLabel = findViewById(R.id.ExpiryLabel); // Initialize expiry label

        // Log the item list to verify OCR text
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

        Log.i("size allergerns", "" + allergenItems.size());
        Log.i("size lactoseItems", "" + lactoseItems.size());
        Log.i("size veganItems", "" + veganItems.size());
        Log.i("size vegetarianItems", "" + vegetarianItems.size());
        Log.i("size glutenItems", "" + glutenItems.size());

        if (noBadIngredients(allergenItems, lactoseItems, veganItems, vegetarianItems, glutenItems)) {
            Log.i("OK", "its a");
            speechText.append("The ingredients are okay.");
            icon.setImageDrawable(check);
        } else {
            Log.i("OK", "its n");
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

        // Check expiry date
        String expiryDate = parser.extractExpiryDate(itemList);
        Log.i("ExpiryDate", "Extracted Expiry Date: " + expiryDate); // Log the extracted expiry date
        checkExpiryDate(expiryDate);

        anotherPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterCaptureActivity.this, OcrCaptureActivity.class);
                intent.putExtra("preferences", preferences);
                startActivity(intent);
            }
        });

        textToSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ts.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    ts.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    private boolean noBadIngredients(ArrayList<ArrayList<String>> a,
                                     ArrayList<String> b,
                                     ArrayList<String> c,
                                     ArrayList<String> d,
                                     ArrayList<String> e) {
        return (a.size() == 0) && (b.size() == 0) && (c.size() == 0) && (d.size() == 0) && (e.size() == 0);
    }

    public void onBackPressed() {
        Intent i = new Intent(AfterCaptureActivity.this, MainActivity.class);
        i.putExtra("preferences", preferences);
        startActivity(i);
        finish();
    }

    private void displayNegativeNested(ArrayList<ArrayList<String>> result) {
        for (int i = 0; i < result.size() - 1; i++) {
            for (int j = 0; j < result.get(i).size(); j++) {
                TextView text = new TextView(this);
                text.setText(result.get(i).get(j));
                text.setTextColor(Color.rgb(209, 89, 98));
                text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                badIngredientsBox.addView(text);
            }
        }
        speechText.append(result.get(result.size() - 1));
        speechText.append(" ");
    }

    private void displayNegative(ArrayList<String> result) {
        for (int i = 0; i < result.size() - 1; i++) {
            TextView text = new TextView(this);
            text.setText(result.get(i));
            text.setTextColor(Color.rgb(209, 89, 98));
            text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            badIngredientsBox.addView(text);
        }
        speechText.append(result.get(result.size() - 1));
        speechText.append(" ");
    }

    private void checkExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            expiryLabel.setText("Expiry Date: Not Found");
            expiryLabel.setBackgroundColor(Color.GRAY);
            Log.i("ExpiryDate", "No expiry date found or invalid format");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long expiryTime = parseExpiryDate(expiryDate);

        if (expiryTime == -1) {
            expiryLabel.setText("Expiry Date: Invalid Format");
            expiryLabel.setBackgroundColor(Color.GRAY);
            Log.i("ExpiryDate", "Invalid expiry date format: " + expiryDate);
            return;
        }

        long difference = expiryTime - currentTime;

        if (difference > 7 * ONE_DAY_IN_MILLIS) {
            expiryLabel.setText("Expiry Date: Fresh (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.GREEN);
            Log.i("ExpiryDate", "Product is fresh: " + expiryDate);
        } else if (difference > 0) {
            expiryLabel.setText("Expiry Date: Near Expiry (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.YELLOW);
            Log.i("ExpiryDate", "Product is near expiry: " + expiryDate);
        } else {
            expiryLabel.setText("Expiry Date: Expired (" + expiryDate + ")");
            expiryLabel.setBackgroundColor(Color.RED);
            Log.i("ExpiryDate", "Product is expired: " + expiryDate);
        }
    }

    private long parseExpiryDate(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = sdf.parse(expiryDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ExpiryDate", "Error parsing expiry date: " + expiryDate);
            return -1;
        }
    }
}