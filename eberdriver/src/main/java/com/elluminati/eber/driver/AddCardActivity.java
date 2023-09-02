package com.elluminati.eber.driver;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.ExpirationDatePickerDialog;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.Utils;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Pattern;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCardActivity extends BaseAppCompatActivity {
    private static final Pattern CODE_PATTERN = Pattern.compile("([0-9]{0,4})|([0-9]{4}-)+|" +
            "([0-9]{4}-[0-9]{0,4})+");
    private EditText etCreditCardNum, etCvc, etMonth, etCardHolderName;
    private String cardType;
    private int cardExpMonth = 0, cardExpYear = 0;
    private Intent intent;
    String registerfee;
    private CustomDialogBigLabel customDialogBigLabel;
    private MyFontButton btnRegisterpay;
    LinearLayout llimagepayment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
       // initToolBar();
      //  etCardHolderName = findViewById(R.id.etCardHolderName);
      //  etCreditCardNum = findViewById(R.id.etCreditCardNumber);
      //  btnRegisterpay = findViewById(R.id.btnRegisterpay);
       // llimagepayment = findViewById(R.id.llimagepayment);
        Button btn_submit= findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCreditCard();
            }
        });

        etCvc = findViewById(R.id.etCvv);
        etMonth = findViewById(R.id.etMonth);
       // setCardTextWatcher();


        intent = getIntent();
        registerfee = intent.getStringExtra("registerfee");
        if (registerfee == null) {
            setTitleOnToolbar(getResources().getString(R.string.text_cards));
            registerfee = "false";
        } else {
            setTitleOnToolbar(getResources().getString(R.string.text_driver_pay));
            btnRegisterpay.setVisibility(View.VISIBLE);
            llimagepayment.setVisibility(View.VISIBLE);
        }

        if (registerfee.equals("registerfee")) {

            btnRegisterpay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCreditCard();
                }
            });
        } else {
            setToolbarIcon(AppCompatResources.getDrawable(this, R.drawable
                    .ic_done_black_24dp), new View
                    .OnClickListener
                    () {
                @Override
                public void onClick(View view) {
                    if (isValidate()) {
                        saveCreditCard();
                    }
                }
            });
        }

        etMonth.setOnClickListener(this);
    }


    protected void openLogoutDialog() {

        customDialogBigLabel = new CustomDialogBigLabel(this, getString(R.string.text_logout),
                getString(R.string.msg_are_you_sure), getString(R.string.text_yes), getString(R
                .string.text_no)) {
            @Override
            public void positiveButton() {
                customDialogBigLabel.dismiss();
                logOut();
            }

            @Override
            public void negativeButton() {
                customDialogBigLabel.dismiss();
            }
        };
        customDialogBigLabel.show();
    }

    @Override
    protected boolean isValidate() {
        String msg = null;
        String month = etMonth.getText().toString();
        if (etCardHolderName.getText().toString().isEmpty()) {
            msg = getString(R.string.msg_please_enter_valid_name);
            etCardHolderName.requestFocus();
        } else if (etCreditCardNum.getText().toString().isEmpty()) {
            msg = getString(R.string.msg_enter_card_number);
            etCreditCardNum.requestFocus();
        } else if (etMonth.getText().toString().isEmpty()) {
            msg = getString(R.string.msg_enter_valid_month);
            etMonth.requestFocus();
        } else if (etCvc.getText().toString().isEmpty()) {
            msg = getString(R.string.msg_enter_cvv);
            etCvc.requestFocus();

        }

        if (msg != null) {
            Utils.showToast(msg, this);
            return false;

        }

        return true;

    }

    @Override
    public void goWithBackArrow() {

        if (preferenceHelper.getIsUserPay() == 0) {
            onBackPressed();
        } else {
            openLogoutDialog();
        }

    }

    @Override
    public void onBackPressed() {
        if (preferenceHelper.getIsUserPay() == 0) {
            super.onBackPressed();
        } else {
            openLogoutDialog();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.etMonth:
                openDialogDateField();
                break;
            default:
                // do with default
                break;
        }
    }

    @Override
    public void onAdminApproved() {

    }

    @Override
    public void onAdminDeclined() {

    }

    @Override
    public void onprofileApprove() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdminApprovedListener(this);
        setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogInternet();
        } else {
            openInternetDialog();
        }
    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogGps();
        } else {
            openGpsDialog();

        }
    }

    /*private void setCardTextWatcher() {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(19);
        etCreditCardNum.setFilters(FilterArray);
        etCreditCardNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etCreditCardNum.getText().toString().length() == 19) {
                    //openDialogDateField();
                }
                if (Utils.isBlank(s.toString())) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                cardType = getCreditCardType(s.toString());

                switch (cardType) {
                    case Card.VISA:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat
                                        .getDrawable(getResources(), R.drawable.ub__creditcard_visa,
                                                null),
                                null, null, null);

                        break;
                    case Card.MASTERCARD:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat
                                .getDrawable(getResources(), R.drawable.ub__creditcard_mastercard,
                                        null), null, null, null);

                        break;
                    case Card.AMERICAN_EXPRESS:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat
                                        .getDrawable(getResources(), R.drawable.ub__creditcard_amex,
                                                null),
                                null, null, null);

                        break;
                    case Card.DISCOVER:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat
                                .getDrawable(getResources(), R.drawable.ub__creditcard_discover,
                                        null), null, null, null);

                        break;
                    case Card.DINERS_CLUB:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat
                                .getDrawable(getResources(), R.drawable.ub__creditcard_discover,
                                        null), null, null, null);

                        break;
                    default:
                        etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                                null);
                        break;
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do with text
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !CODE_PATTERN.matcher(s).matches()) {
                    String input = s.toString();
                    String numbersOnly = keepNumbersOnly(input);
                    String code = formatNumbersAsCode(numbersOnly);
                    etCreditCardNum.removeTextChangedListener(this);
                    etCreditCardNum.setText(code);
                    etCreditCardNum.setSelection(code.length());
                    etCreditCardNum.addTextChangedListener(this);
                }
            }

            private String keepNumbersOnly(CharSequence s) {
                return s.toString().replaceAll("[^0-9]", ""); // Should of
                // course be
                // more robust
            }

            private String formatNumbersAsCode(CharSequence s) {
                int groupDigits = 0;
                String tmp = "";
                int sSize = s.length();
                for (int i = 0; i < sSize; ++i) {
                    tmp += s.charAt(i);
                    ++groupDigits;
                    if (groupDigits == 4) {
                        tmp += "-";
                        groupDigits = 0;
                    }
                }
                return tmp;
            }
        });

        etMonth.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do with text
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do with text
            }
        });


    }*/

   /* private String getCreditCardType(String number) {
        if (!Utils.isBlank(number)) {
            if (Utils.hasAnyPrefix(number, Card
                    .PREFIXES_AMERICAN_EXPRESS)) {
                return Card.AMERICAN_EXPRESS;
            } else if (Utils.hasAnyPrefix(number, Card
                    .PREFIXES_DISCOVER)) {
                return Card.DISCOVER;
            } else if (Utils.hasAnyPrefix(number, Card.PREFIXES_JCB)) {
                return Card.JCB;
            } else if (Utils.hasAnyPrefix(number, Card
                    .PREFIXES_DINERS_CLUB)) {
                return Card.DINERS_CLUB;
            } else if (Utils.hasAnyPrefix(number, Card.PREFIXES_VISA)) {
                return Card.VISA;
            } else if (Utils.hasAnyPrefix(number, Card
                    .PREFIXES_MASTERCARD)) {
                return Card.MASTERCARD;
            } else {
                return Card.UNKNOWN;
            }
        }
        return Card.UNKNOWN;

    }*/

    private void saveCreditCard() {
        CardInputWidget cardInputWidget = findViewById(R.id.card_input_widget);
        Card card = cardInputWidget.getCard();

        if (card != null) {
            // Stripe stripe = new Stripe(getApplicationContext(), "your_publishable_key");
            Stripe stripe = new Stripe(this, preferenceHelper.getStripePublicKey());


            PaymentMethodCreateParams params = PaymentMethodCreateParams.create(
                    cardInputWidget.getPaymentMethodCard()
            );

            stripe.createPaymentMethod(params, new ApiResultCallback<PaymentMethod>() {
                @Override
                public void onSuccess(PaymentMethod paymentMethod) {
                    // Handle successful creation
                    String paymentMethodId = paymentMethod.id;
                    // AppLog.Log("CARD_COUNTRY1", paymentMethod.toString());
                    AppLog.Log("CARD_COUNTRY", paymentMethodId);
                    addCard(paymentMethodId);
                }

                @Override
                public void onError(@NotNull Exception e) {
                    // Handle error
                }
            });
            // Card information is valid, you can proceed with creating a Payment Method
            // You can also access individual card details like card.getNumber(), card.getExpMonth(), etc.
        } else {
            // Card information is invalid, show an error to the user
        }


        /*Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_add_card), false, null);

        Card card = new Card(etCreditCardNum.getText().toString(), cardExpMonth, cardExpYear, etCvc
                .getText().toString());
        boolean validation = card.validateCard();
        if (validation) {

            AppLog.Log("STRIPE_KEY", preferenceHelper.getStripePublicKey());
            Stripe stripe = new Stripe(this, preferenceHelper.getStripePublicKey());
            stripe.createToken(card, preferenceHelper.getStripePublicKey(), new
                    TokenCallback() {
                        public void onSuccess(Token token) {
                            AppLog.Log("CARD_CURRENCY", token.getCard().getCurrency());
                            AppLog.Log("CARD_COUNTRY", token.getCard().getCountry());
                            String lastFour = token.getCard().getLast4();

                            if (registerfee.equals("registerfee")) {
                                addCardforpay(token.getId(), lastFour, cardType);
                            } else {
                                addCard(token.getId(), lastFour, cardType);
                            }

                        }

                        public void onError(Exception error) {
                            Utils.showToast(getString(R.string.text_error), AddCardActivity.this);
                            Utils.hideCustomProgressDialog();
                        }
                    });
        } else if (!card.validateNumber()) {
            // handleError("The card number that you entered is invalid");
            Utils.showToast(getString(R.string.msg_number_invalid), this);
            Utils.hideCustomProgressDialog();
        } else if (!card.validateExpiryDate()) {
            // handleError("");
            Utils.showToast(getString(R.string.msg_date_invalid), this);
            Utils.hideCustomProgressDialog();
        } else if (!card.validateCVC()) {
            // handleError("");
            Utils.showToast(getString(R.string.msg_cvc_invalid), this);
            Utils.hideCustomProgressDialog();

        } else {
            // handleError("");
            Utils.showToast(getString(R.string.msg_card_invalid), this);
            Utils.hideCustomProgressDialog();
        }*/
    }

    private void addCard(String paymentToken) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            /*jsonObject.put(Const.Params.PAYMENT_TOKEN, paymentToken);
            //jsonObject.put(Const.Params.LAST_FOUR, lastFour);
            jsonObject.put(Const.Params.CARD_TYPE, cardType);
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());*/
            jsonObject.put(Const.Params.PAYMENT_METHOD, paymentToken);
            // jsonObject.put(Const.Params.LAST_FOUR, lastFour);
            //jsonObject.put(Const.Params.CARD_TYPE, cardType);
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());
            AppLog.Log("CARD_COUNTRY11", paymentToken);
            AppLog.Log("CARD_COUNTRY22", preferenceHelper.getSessionToken());


            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class).addCard
                    (ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        if (response.body().isSuccess()) {
                            setResult(Activity.RESULT_OK);

                            preferenceHelper.putIsPayUser(response.body().getIspaidforregister());

                            if (registerfee.equals("registerfee")) {
                                moveWithUserSpecificPreference();

                            }

                            finish();
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), AddCardActivity
                                    .this);
                        }
                    }
                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(PaymentActivity.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.VIEW_AND_ADD_PAYMENT_ACTIVITY, e);
        }

    }


    private void addCardforpay(String paymentToken, String lastFour, String cardType) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            jsonObject.put(Const.Params.PAYMENT_TOKEN, paymentToken);
            jsonObject.put(Const.Params.LAST_FOUR, lastFour);
            jsonObject.put(Const.Params.CARD_TYPE, cardType);
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class).addCardforpay
                    (ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        if (response.body().isSuccess()) {
                            setResult(Activity.RESULT_OK);

                            preferenceHelper.putIsPayUser(response.body().getIspaidforregister());

                            //    if (registerfee.equals("registerfee")) {
                            moveWithUserSpecificPreference();

                            //  }

                            finish();
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), AddCardActivity
                                    .this);
                        }
                    }
                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(PaymentActivity.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.VIEW_AND_ADD_PAYMENT_ACTIVITY, e);
        }

    }


    private void openDialogDateField() {
        Calendar calendar = Calendar.getInstance();
        ExpirationDatePickerDialog dp = new ExpirationDatePickerDialog(this, new DatePickerDialog
                .OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear,
                                  int dayOfMonth) {
                cardExpMonth = monthOfYear + 1;
                cardExpYear = year;
                etMonth.setText(cardExpMonth + "/" + cardExpYear);
                etCvc.requestFocus();
            }
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dp.show();


    }
}
