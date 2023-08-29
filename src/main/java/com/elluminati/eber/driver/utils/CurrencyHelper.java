package com.elluminati.eber.driver.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyHelper {
    private static CurrencyHelper currencyHelper;
    private String currencyCode = "";
    private String countryCode = "US";
    private String language = "en";
    private PreferenceHelper preferenceHelper;
    private Locale localeFinal;

    private CurrencyHelper(Context context) {
        preferenceHelper = PreferenceHelper.getInstance(context);
    }

    public static CurrencyHelper getInstance(Context context) {
        if (currencyHelper == null) {
            currencyHelper = new CurrencyHelper(context);
            return currencyHelper;
        } else {
            return currencyHelper;
        }
    }

    public NumberFormat getCurrencyFormat(String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            if (localeFinal == null) {
                localeFinal = new Locale(language,
                        countryCode);
            }
            return NumberFormat.getCurrencyInstance(localeFinal);
        }
        Locale locale = null;
        if (!TextUtils.equals(this.currencyCode, currencyCode)) {
            for (Locale availableLocale : NumberFormat.getAvailableLocales()) {
                String code =
                        NumberFormat.getCurrencyInstance(availableLocale).getCurrency().getCurrencyCode();
                if (TextUtils.equals(currencyCode, code)) {
                    locale = availableLocale;
                    this.currencyCode = currencyCode;
                    break;
                }

            }
            if (locale != null) {
                String localCode = locale.toString();
                String[] localCodeSplitArray;

                if (localCode.contains("_")) {
                    localCodeSplitArray = localCode.split("_");
                    if (localCodeSplitArray.length > 0) {
                        countryCode = localCodeSplitArray[localCodeSplitArray.length - 1];
                    }
                } else {
                    countryCode = localCode;
                }
            }
            localeFinal=new Locale(language,countryCode);

        }
        if (!TextUtils.equals(language, preferenceHelper.getLanguageCode())) {
            language = preferenceHelper.getLanguageCode();
            localeFinal=new Locale(language,countryCode);
        }

        return NumberFormat.getCurrencyInstance(localeFinal);

    }
}
