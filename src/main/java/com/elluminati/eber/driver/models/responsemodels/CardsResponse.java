package com.elluminati.eber.driver.models.responsemodels;

import com.elluminati.eber.driver.models.datamodels.Card;
import com.elluminati.eber.driver.models.datamodels.PaymentGateway;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardsResponse {

    @SerializedName("is_use_wallet")
    private int isUseWallet;
    @SerializedName("wallet")
    private double wallet;
    @SerializedName("payment_gateway")
    private List<PaymentGateway> paymentGateway;
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("wallet_currency_code")
    private String walletCurrencyCode;
    @SerializedName("card")
    private List<Card> card;
    @SerializedName("error_code")
    private int errorCode;

    public int getIsUseWallet() {
        return isUseWallet;
    }

    public void setIsUseWallet(int isUseWallet) {
        this.isUseWallet = isUseWallet;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public List<PaymentGateway> getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(List<PaymentGateway> paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWalletCurrencyCode() {
        return walletCurrencyCode;
    }

    public void setWalletCurrencyCode(String walletCurrencyCode) {
        this.walletCurrencyCode = walletCurrencyCode;
    }

    public List<Card> getCard() {
        return card;
    }

    public void setCard(List<Card> card) {
        this.card = card;
    }

    @Override
    public String toString() {
        return
                "CardsResponse{" +
                        "wallet = '" + wallet + '\'' +
                        ",payment_gateway = '" + paymentGateway + '\'' +
                        ",success = '" + success + '\'' +
                        ",message = '" + message + '\'' +
                        ",wallet_currency_code = '" + walletCurrencyCode + '\'' +
                        ",card = '" + card + '\'' +
                        "}";
    }
}