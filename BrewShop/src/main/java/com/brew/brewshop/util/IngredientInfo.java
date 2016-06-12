package com.brew.brewshop.util;

import android.content.Context;

import com.brew.brewshop.R;
import com.brew.brewshop.storage.recipes.HopAddition;
import com.brew.brewshop.storage.recipes.MaltAddition;
import com.brew.brewshop.storage.recipes.Recipe;
import com.brew.brewshop.storage.recipes.Yeast;

public class IngredientInfo {
    private static final double MIN_MALT_WEIGHT = 0.0001; //ounces

    public static String getInfo(Context context, MaltAddition addition, Recipe recipe) {
        double total = recipe.getTotalMaltWeight().getOunces();
        if (total < MIN_MALT_WEIGHT) {
            total = MIN_MALT_WEIGHT;
        }
        double percent = 100 * addition.getWeight().getOunces() / total;
        return Util.fromDouble(percent, 1, true) + context.getString(R.string.perc_of_grist);
    }

    public static String getInfo(Context context, HopAddition addition) {
        String info = "";
        switch (addition.getUsage()) {
            case FIRST_WORT:
                info = context.getString(R.string.first_wort_hop);
                break;
            case BOIL:
                int minutes = addition.getBoilTime();
                info = String.format(context.getString(R.string.minute_to_add), minutes);
                break;
            case WHIRLPOOL:
                info = context.getString(R.string.whirlpool);
                break;
            case DRY_HOP:
                info = String.format(context.getString(R.string.dry_hop_n_days), addition.getDryHopDays());
                break;
        }
        return info;
    }

    public static String getInfo(Context context, Yeast yeast) {
        double attenuation = yeast.getAttenuation();
        return "~" + Util.fromDouble(attenuation, 1, true) + context.getString(R.string.perc_attenuation);
    }
}
