package com.example.smartfoods;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {
    private static final String TAG = "TextParser";

    public ArrayList<ArrayList<String>> allAllergens;
    public ArrayList<String> userAllergens = new ArrayList<>();
    public ArrayList<String> allLactose;
    public ArrayList<String> allVegan;
    public ArrayList<String> allVegetarian;
    public ArrayList<String> allGluten;
    public ArrayList<String> allArtificialSweeteners;
    public ArrayList<String> allHarmfulAdditives;
    public ArrayList<String> allArtificialColors;
    public ArrayList<String> allHighSodium;
    public ArrayList<String> allSugaryAdditives;
    public ArrayList<String> allUnhealthyFats;
    public ArrayList<String> nova1;
    public ArrayList<String> nova2;
    public ArrayList<String> nova3;
    public ArrayList<String> nova4;

    private static final String[] DATE_FORMATS = {
            "dd/MM/yy", "dd/MM/yyyy", "MM/dd/yy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy",
            "dd.MM.yyyy", "MM.dd.yyyy", "yyyy.MM.dd", "dd MMM yyyy", "dd MMMM yyyy", "MMM dd, yyyy",
            "yyyy/MM/dd", "dd-MMM-yyyy", "MMM-dd-yyyy", "dd MMM yy", "MMM dd yy", "MMM yyyy",
            "MM/yy", "MM/yyyy"
    };

    private static final String[] DATE_KEYWORDS = {
            "expiry", "best before", "use by", "best by", "expires", "expiration", "valid until"
    };

    public TextParser() {
        initializeIngredientLists();
    }
    private String buildDateRegexPattern() {
        return "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b|" +
                "\\b(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})\\b|" +
                "\\b(\\d{1,2}\\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s\\d{2,4})\\b";
    }

    private boolean containsDateKeyword(String line) {
        for (String keyword : DATE_KEYWORDS) {
            if (line.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Date parseDateWithMultipleFormats(String dateStr) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    public String extractExpiryDate(ArrayList<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "Not Found";
        }

        List<Date> foundDates = new ArrayList<>();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String dateRegex = buildDateRegexPattern();

        Pattern pattern = Pattern.compile(dateRegex, Pattern.CASE_INSENSITIVE);

        for (String line : ingredients) {
            if (line == null) continue;

            boolean hasDateKeyword = containsDateKeyword(line.toLowerCase());
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                String dateStr = matcher.group();
                Date date = parseDateWithMultipleFormats(dateStr);

                if (date != null) {
                    if (hasDateKeyword) {
                        return outputFormat.format(date);
                    }
                    foundDates.add(date);
                }
            }
        }

        if (!foundDates.isEmpty()) {
            Collections.sort(foundDates);
            return outputFormat.format(foundDates.get(foundDates.size() - 1));
        }

        return "Not Found";
    }

    private void initializeIngredientLists() {
        this.allAllergens = fillInAllergens();

        this.allLactose = new ArrayList<>(Arrays.asList(
                "milk", "butter", "buttermilk", "casein", "cheese", "cream",
                "curds", "lactose", "lactulose", "lactate", "custard", "yogurt", "paneer", "whey", "ghee"
        ));

        this.allVegan = new ArrayList<>(Arrays.asList(
                "egg", "milk", "butter", "cream", "cheese", "meat", "pork", "chicken", "beef",
                "lamb", "veal", "fish", "seafood", "shrimp", "lobster", "gelatin", "honey", "albumin"
        ));

        this.allVegetarian = new ArrayList<>(Arrays.asList(
                "meat", "pork", "chicken", "beef", "lamb", "veal", "fish", "seafood", "shrimp",
                "lobster", "anchovy", "bacon", "ham", "gelatin"
        ));

        this.allGluten = new ArrayList<>(Arrays.asList(
                "wheat", "barley", "rye", "malt", "farina", "semolina", "spelt", "couscous",
                "triticale", "durum", "einkorn", "flour"
        ));

        this.allArtificialSweeteners = new ArrayList<>(Arrays.asList(
                "aspartame", "acesulfame potassium", "sucralose", "saccharin", "neotame", "stevia",
                "erythritol", "xylitol", "sorbitol", "maltitol", "mannitol", "isomalt"
        ));

        this.allHarmfulAdditives = new ArrayList<>(Arrays.asList(
                "monosodium glutamate", "msg", "sodium nitrite", "sodium nitrate", "bht", "bha",
                "potassium bromate", "sodium benzoate", "benzoic acid", "propyl gallate",
                "sorbic acid", "propylene glycol", "artificial flavor", "artificial flavour",
                "tert-butylhydroquinone", "tbqh", "disodium inosinate", "disodium guanylate"
        ));

        this.allArtificialColors = new ArrayList<>(Arrays.asList(
                "red 40", "blue 1", "blue 2", "yellow 5", "yellow 6", "green 3", "orange b",
                "citrus red 2", "allura red", "tartrazine", "sunset yellow", "brilliant blue", "erythrosine",
                "carmine", "ponceau 4r", "caramel color"
        ));

        this.allHighSodium = new ArrayList<>(Arrays.asList(
                "sodium chloride", "disodium inosinate", "disodium guanylate", "monosodium glutamate",
                "sodium phosphate", "sodium caseinate", "sodium bicarbonate", "baking soda"
        ));

        this.allSugaryAdditives = new ArrayList<>(Arrays.asList(
                "sugar", "fructose", "glucose", "high fructose corn syrup", "maltose",
                "corn syrup", "invert sugar", "sucrose", "dextrose", "honey", "molasses"
        ));

        this.allUnhealthyFats = new ArrayList<>(Arrays.asList(
                "partially hydrogenated oil", "shortening", "lard", "palm oil", "palm kernel oil",
                "vegetable shortening", "margarine"
        ));

        this.nova1 = new ArrayList<>(Arrays.asList(
                "oats", "whole wheat", "brown rice", "lentils", "beans", "fruits", "vegetables",
                "eggs", "milk", "yogurt", "fish", "chicken", "spices", "herbs", "nuts", "quinoa", "tofu"
        ));

        this.nova2 = new ArrayList<>(Arrays.asList(
                "vegetable oil", "sunflower oil", "salt", "sugar", "ghee", "butter", "coconut oil",
                "corn starch", "potato starch", "tapioca starch", "vinegar"
        ));

        this.nova3 = new ArrayList<>(Arrays.asList(
                "jam", "pickle", "bread", "cheese", "canned beans", "salted nuts", "processed meats",
                "salt", "sugar", "palm oil", "canola oil", "sunflower oil", "glucose", "sucrose", "brown sugar"
        ));

        this.nova4 = new ArrayList<>(Arrays.asList(
                "emulsifier", "stabilizer", "thickener", "xanthan gum", "guar gum", "lecithin",
                "polysorbate 80", "pectin", "gum arabic", "gellan gum", "agar", "yeast extract",
                "flavoring", "natural flavor", "artificial flavor", "color", "modified starch",
                "hydrolyzed vegetable protein", "hvp", "monosodium glutamate", "aspartame", "sucralose",
                "corn syrup", "high fructose corn syrup", "maltodextrin", "dextrose", "caramel color"
        ));
    }

    public void setUserPreferences(String input) {
        userAllergens.clear();
        if (input == null || input.length() < 10) {
            for (int i = 0; i < 10; i++) userAllergens.add("0");
            return;
        }
        for (int i = 0; i < 10; i++) userAllergens.add(String.valueOf(input.charAt(i)));
    }

    public ArrayList<ArrayList<String>> fillInAllergens() {
        ArrayList<ArrayList<String>> allergens = new ArrayList<>();

        allergens.add(new ArrayList<>(Arrays.asList("milk", "butter", "casein", "cheese", "cream", "curds", "lactose", "custard", "yogurt")));
        allergens.add(new ArrayList<>(Arrays.asList("egg", "eggs", "albumin", "globulin", "lysozyme")));
        allergens.add(new ArrayList<>(Arrays.asList("peanut", "nuts", "almond", "walnut", "cashew", "pecan", "hazelnut", "pistachio")));
        allergens.add(new ArrayList<>(Arrays.asList("wheat", "flour", "barley", "cereal", "cracker")));
        allergens.add(new ArrayList<>(Arrays.asList("soy", "soya", "tofu", "miso", "edamame")));
        allergens.add(new ArrayList<>(Arrays.asList("fish", "anchovy", "shrimp", "crab", "lobster", "tuna", "salmon")));

        return allergens;
    }

    public ArrayList<String> processInput(ArrayList<String> ingredients) {
        ArrayList<String> output = new ArrayList<>();
        if (ingredients == null) return output;

        for (String line : ingredients) {
            if (line == null) continue;
            String[] parts = line.toLowerCase().split("[,;\\s]+");
            for (String part : parts) {
                output.add(part.replaceAll("[^a-zA-Z0-9\\-]", ""));
            }
        }
        return output;
    }

    public ArrayList<ArrayList<String>> checkAllergens(ArrayList<String> ingredients) {
        ArrayList<ArrayList<String>> returnList = new ArrayList<>();
        if (ingredients == null || userAllergens.size() < 6) return returnList;

        ArrayList<String> allIngredients = processInput(ingredients);
        ArrayList<String> mapping = new ArrayList<>(Arrays.asList(
                "milk allergen(s)", "egg allergen(s)", "peanut/nut allergen(s)",
                "wheat allergen(s)", "soy allergen(s)", "seafood allergen(s)"
        ));

        for (int index = 0; index < 6; index++) {
            if (userAllergens.get(index).equals("1")) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add("Possible " + mapping.get(index));

                for (String allergen : allAllergens.get(index)) {
                    for (String ingredient : allIngredients) {
                        if (ingredient.contains(allergen) && !temp.contains(allergen)) {
                            temp.add(allergen);
                        }
                    }
                }

                if (temp.size() > 1) {
                    returnList.add(temp);
                }
            }
        }

        return returnList;
    }

    public ArrayList<String> checkLactose(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null || userAllergens.size() < 7 || !userAllergens.get(6).equals("1")) {
            return foundItems;
        }

        ArrayList<String> allIngredients = processInput(ingredients);
        for (String ingredient : allIngredients) {
            for (String item : allLactose) {
                if (ingredient.contains(item) && !foundItems.contains(item)) {
                    foundItems.add(item);
                }
            }
        }

        return foundItems;
    }

    public ArrayList<String> checkVegan(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null || userAllergens.size() < 8 || !userAllergens.get(7).equals("1")) {
            return foundItems;
        }

        ArrayList<String> allIngredients = processInput(ingredients);
        for (String ingredient : allIngredients) {
            for (String item : allVegan) {
                if (ingredient.contains(item) && !foundItems.contains(item)) {
                    foundItems.add(item);
                }
            }
        }

        return foundItems;
    }

    public ArrayList<String> checkVegetarian(ArrayList<String> ingredients) {
        ArrayList<String> nonVegetarianItems = new ArrayList<>();
        if (ingredients == null) {
            return nonVegetarianItems;
        }

        for (String ingredient : ingredients) {
            if (ingredient == null) continue;

            String lowerIngredient = ingredient.toLowerCase();
            for (String meatItem : allVegetarian) {
                if (lowerIngredient.contains(meatItem.toLowerCase())) {
                    if (!nonVegetarianItems.contains(meatItem)) {
                        nonVegetarianItems.add(meatItem);
                    }
                }
            }
        }
        return nonVegetarianItems;
    }
    public ArrayList<String> checkGluten(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null || userAllergens.size() < 10 || !userAllergens.get(9).equals("1")) {
            return foundItems;
        }

        ArrayList<String> allIngredients = processInput(ingredients);
        for (String ingredient : allIngredients) {
            for (String item : allGluten) {
                if (ingredient.contains(item) && !foundItems.contains(item)) {
                    foundItems.add(item);
                }
            }
        }

        return foundItems;
    }

    public ArrayList<String> checkArtificialSweeteners(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String sweetener : allArtificialSweeteners) {
                if (lowerLine.contains(sweetener.toLowerCase()) && !foundItems.contains(sweetener)) {
                    foundItems.add(sweetener);
                }
            }
        }
        return foundItems;
    }

    public ArrayList<String> checkHarmfulAdditives(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String additive : allHarmfulAdditives) {
                if (lowerLine.contains(additive.toLowerCase()) && !foundItems.contains(additive)) {
                    foundItems.add(additive);
                }
            }
        }
        return foundItems;
    }

    public ArrayList<String> checkUnhealthyFats(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String fat : allUnhealthyFats) {
                if (lowerLine.contains(fat.toLowerCase()) && !foundItems.contains(fat)) {
                    foundItems.add(fat);
                }
            }
        }
        return foundItems;
    }

    public ArrayList<String> checkArtificialColors(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String color : allArtificialColors) {
                if (lowerLine.contains(color.toLowerCase()) && !foundItems.contains(color)) {
                    foundItems.add(color);
                }
            }
        }
        return foundItems;
    }

    public ArrayList<String> checkHighSodium(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String sodiumItem : allHighSodium) {
                if (lowerLine.contains(sodiumItem.toLowerCase()) && !foundItems.contains(sodiumItem)) {
                    foundItems.add(sodiumItem);
                }
            }
        }
        return foundItems;
    }

    public ArrayList<String> checkSugaryAdditives(ArrayList<String> ingredients) {
        ArrayList<String> foundItems = new ArrayList<>();
        if (ingredients == null) return foundItems;

        for (String line : ingredients) {
            if (line == null) continue;
            String lowerLine = line.toLowerCase();
            for (String sugar : allSugaryAdditives) {
                if (lowerLine.contains(sugar.toLowerCase()) && !foundItems.contains(sugar)) {
                    foundItems.add(sugar);
                }
            }
        }
        return foundItems;
    }

    public String determineNovaCategory(ArrayList<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "No ingredients to analyze";
        }

        ArrayList<String> allIngredients = processInput(ingredients);
        int[] novaCounts = new int[4]; // Count for each NOVA category

        for (String ingredient : allIngredients) {
            if (nova1.contains(ingredient)) {
                novaCounts[0]++;
            } else if (nova2.contains(ingredient)) {
                novaCounts[1]++;
            } else if (nova3.contains(ingredient)) {
                novaCounts[2]++;
            } else if (nova4.contains(ingredient)) {
                novaCounts[3]++;
            }
        }

        // Determine the dominant category
        int maxIndex = 0;
        for (int i = 1; i < novaCounts.length; i++) {
            if (novaCounts[i] > novaCounts[maxIndex]) {
                maxIndex = i;
            }
        }

        switch (maxIndex) {
            case 0: return "NOVA 1 (Unprocessed/Minimally Processed)";
            case 1: return "NOVA 2 (Processed Culinary Ingredients)";
            case 2: return "NOVA 3 (Processed Foods)";
            case 3: return "NOVA 4 (Ultra-Processed Foods)";
            default: return "Unable to determine NOVA category";
        }
    }


}