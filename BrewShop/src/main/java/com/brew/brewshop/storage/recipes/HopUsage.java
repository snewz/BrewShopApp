package com.brew.brewshop.storage.recipes;

import com.brew.brewshop.R;

public enum HopUsage {
    // keep synchronized with: R.array.hops_usage!
    FIRST_WORT("First Wort"),
    BOIL("Boil"),
    WHIRLPOOL("Whirlpool"),
    DRY_HOP("Dry Hop"),
    MASH("Mash");

    private String mText;

    public final static int RESOURCE_ARRAY = R.array.hops_usage;

    HopUsage(String value) {
        mText = value;
    }

    public String getText() {
        return mText;
    }

    public static HopUsage fromString(String text) {
        if (text != null) {
            for (HopUsage b : HopUsage.values()) {
                if (text.equalsIgnoreCase(b.getText())) {
                    return b;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}