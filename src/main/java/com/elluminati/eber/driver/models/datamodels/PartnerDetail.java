package com.elluminati.eber.driver.models.datamodels;

import com.google.gson.annotations.SerializedName;

public class PartnerDetail {

    @SerializedName("wallet")
    private double wallet;

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public double getWallet() {
        return wallet;
    }

    @Override
    public String toString() {
        return
                "PartnerDetail{" +
                        "wallet = '" + wallet + '\'' +
                        "}";
    }
}