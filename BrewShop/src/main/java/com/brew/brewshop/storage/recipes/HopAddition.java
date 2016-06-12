package com.brew.brewshop.storage.recipes;

import android.os.Parcel;
import android.os.Parcelable;

public class HopAddition implements Parcelable {
    private int minutes;
    private Hop hop;
    private Weight weight;
    private HopUsage usage;
    private int dryhop_days;

    public HopAddition() {
        weight = new Weight();
        hop = new Hop();
        minutes = 60;
        dryhop_days = 5;
        usage = HopUsage.BOIL;
    }

    public HopAddition(Parcel parcel) {
        minutes = parcel.readInt();
        hop = parcel.readParcelable(Hop.class.getClassLoader());
        weight = parcel.readParcelable(Weight.class.getClassLoader());
        usage = HopUsage.fromString(parcel.readString());
    }

    public Hop getHop() { return hop; }
    public void setHop(Hop value) { hop = value; }

    public Weight getWeight() { return weight; }
    public void setWeight(Weight value) { weight = value; }

    public int getBoilTime() { return minutes; }
    public void setBoilTime(int value) { minutes = value; }

    public int getDryHopDays() { return dryhop_days; }
    public void setDryHopDays(int value) { dryhop_days = value; }

    public HopUsage getUsage() { return usage; }
    public void setUsage(HopUsage value) { usage = value; }

    public static final Parcelable.Creator<HopAddition> CREATOR = new Parcelable.Creator<HopAddition>() {
        public HopAddition createFromParcel(Parcel in) {
            return new HopAddition(in);
        }
        public HopAddition[] newArray(int size) {
            return new HopAddition[size];
        }
    };

    public boolean equals(HopAddition other) {
        return hop.equals(other.getHop()) && weight.getOunces() == other.getWeight().getOunces()
                && minutes == other.getBoilTime() && dryhop_days == other.getDryHopDays()
                && getUsage() == other.getUsage();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(minutes);
        parcel.writeParcelable(hop, 0);
        parcel.writeParcelable(weight, 0);
        parcel.writeString(usage.getText());
    }


}
