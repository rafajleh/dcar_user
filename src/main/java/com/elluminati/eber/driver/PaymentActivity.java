package com.elluminati.eber.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.elluminati.eber.driver.adapter.CardAdapter;
import com.elluminati.eber.driver.adapter.PaymentAdapter;
import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyTitleFontTextView;
import com.elluminati.eber.driver.models.datamodels.Card;
import com.elluminati.eber.driver.models.datamodels.PaymentGateway;
import com.elluminati.eber.driver.models.responsemodels.AddWalletResponse;
import com.elluminati.eber.driver.models.responsemodels.CardsResponse;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends BaseAppCompatActivity {
    private MyTitleFontTextView tvNoItem;
    private MyFontButton btnAddCard, btnWalletHistory;
    private MyFontTextView
            tvWalletAmount, tvSubmitWalletAmount;
    private LinearLayout llSubmitWalletAmount, llAddButton;
    private MyFontEdittextView etWalletAmount;
    private RecyclerView rcvCards;
    private CardAdapter cardAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Card> cardList;
    private int selectedCardPosition = 0;
    private boolean isClickedOnDrawer;
    private Card selectedCard;
    private Spinner spinnerPayment;
    private ArrayList<PaymentGateway> paymentGatewaysList;
    private PaymentAdapter paymentAdapter;
    private boolean isFromInvoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_payments));
        setToolbarBackgroundAndElevation(false, R.color.color_white, R.dimen.toolbar_elevation);
        tvNoItem = (MyTitleFontTextView) findViewById(R.id.tvNoItem);
        btnAddCard = (MyFontButton) findViewById(R.id.btnAddCard);
        btnAddCard.setOnClickListener(this);
        tvWalletAmount = (MyFontTextView) findViewById(R.id.tvWalletAmount);
        tvSubmitWalletAmount = (MyFontTextView) findViewById(R.id.tvSubmitWalletAmount);
        tvSubmitWalletAmount.setOnClickListener(this);
        llSubmitWalletAmount = (LinearLayout) findViewById(R.id.llSubmitWalletAmount);
        etWalletAmount = (MyFontEdittextView) findViewById(R.id.etWalletAmount);
        spinnerPayment = (Spinner) findViewById(R.id.spinnerPaymentGateWay);
        btnWalletHistory = findViewById(R.id.btnWalletHistory);
        llAddButton = findViewById(R.id.llAddButton);
        btnWalletHistory.setOnClickListener(this);
        cardList = new ArrayList<>();
        initCardList();
        initPaymentGatewaySpinner();
        getCreditCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdminApprovedListener(this);
        setConnectivityListener(this);
    }

    @Override
    public void onBackPressed() {
        if (llSubmitWalletAmount.getVisibility() == View.VISIBLE) {
            updateUiForWallet(false);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    protected boolean isValidate() {

        return false;

    }

    @Override
    public void goWithBackArrow() {
        hideKeyPad();
        onBackPressed();


    }

    public void hideKeyPad() {
        if(etWalletAmount.hasFocus()==true)
        {
            hideKeyBord();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnWalletHistory:
                goToWalletHistoryActivity();
                break;
            case R.id.tvSubmitWalletAmount:
                if (llSubmitWalletAmount.getVisibility() == View.VISIBLE) {
                    hideKeyBord();
                    try {
                        if (selectedCard != null) {
                            if (!android.text.TextUtils.isEmpty(etWalletAmount.getText().toString())
                                    && !android.text.TextUtils.equals(etWalletAmount.getText()
                                    .toString(), "0")) {
                                addWalletAmount(Double.valueOf(etWalletAmount.getText().toString()),
                                        "", selectedCard.getId());

                            } else {
                                Utils.showToast(getResources().getString(R.string
                                        .msg_plz_enter_valid), this);
                            }
                        } else {
                            Utils.showToast(getResources().getString(R.string
                                    .msg_plz_add_credit_card), this);
                        }
                    } catch (NumberFormatException e) {
                        Utils.showToast(getResources().getString(R.string.msg_plz_enter_valid),
                                this);
                    }
                } else {
                    updateUiForWallet(true);
                }
                break;
            case R.id.btnAddCard:
                startActivityForResult(new Intent(this, AddCardActivity.class), Const
                        .REQUEST_ADD_CARD);
                break;
            default:
                // do with default
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_ADD_CARD && resultCode == Activity.RESULT_OK) {
            getCreditCard();
        }

    }

    /***
     * this method used set textWatcher on card editText
     */

    private void getCreditCard() {
        Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_getting_credit_cards), false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());

            Call<CardsResponse> call = ApiClient.getClient().create(ApiInterface.class).getCards
                    (ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<CardsResponse>() {
                @Override
                public void onResponse(Call<CardsResponse> call, Response<CardsResponse> response) {
                    if (parseContent.isSuccessful(response)) {

                        CardsResponse cardsResponse = response.body();
                        if (cardsResponse.isSuccess()) {
                            cardList.clear();
                            cardList.addAll(cardsResponse.getCard());
                            paymentGatewaysList.clear();
                            paymentGatewaysList.addAll(cardsResponse.getPaymentGateway());
                            paymentAdapter.notifyDataSetChanged();
                            String wallet = parseContent.twoDigitDecimalFormat
                                    .format(cardsResponse.getWallet()) + " " +
                                    cardsResponse.getWalletCurrencyCode();
                            tvWalletAmount.setText(wallet);
                            int cardListSize = cardList.size();
                            if (cardListSize > 0) {
                                updateUiCardList(true);
                                for (int i = 0; i < cardListSize; i++) {
                                    if (Const.ProviderStatus.IS_DEFAULT == cardList.get(i)
                                            .getIsDefault()) {
                                        selectCardDataModify(i);
                                        break;
                                    }
                                }
                            } else {
                                updateUiCardList(false);
                            }
                            Utils.hideCustomProgressDialog();
                        } else {
                            Utils.hideCustomProgressDialog();
                        }

                    }

                }



                @Override
                public void onFailure(Call<CardsResponse> call, Throwable t) {
                    AppLog.handleThrowable(PaymentActivity.class.getSimpleName(), t);
                }



            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.VIEW_AND_ADD_PAYMENT_ACTIVITY, e);
        }
    }

    /**
     * This method is used to set card data list from  web service to our recycle view
     * and also getItem on Touch
     */

    private void initCardList() {

        linearLayoutManager = new LinearLayoutManager(this);
        rcvCards = (RecyclerView) findViewById(R.id.card_recycler_view);
        rcvCards.setHasFixedSize(true);
        rcvCards.setLayoutManager(linearLayoutManager);
        cardAdapter = new CardAdapter(this, cardList) {
            @Override
            public void onSelected(int position) {
                selectedCardPosition = position;
                selectCard(cardList.get(position).getId());
            }

            @Override
            public void onClickRemove(int position) {
                openDeleteCard(position);

            }
        };
        rcvCards.setAdapter(cardAdapter);


    }

    private void updateUiCardList(boolean isUpdate) {
        if (isUpdate) {
            tvNoItem.setVisibility(View.GONE);
            rcvCards.setVisibility(View.VISIBLE);
        } else {
            tvNoItem.setVisibility(View.VISIBLE);
            rcvCards.setVisibility(View.GONE);
        }


    }


    private void selectCard(String cardId) {
        Utils.showCustomProgressDialog(this, getResources().getString(R.string.msg_loading),
                false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.CARD_ID, cardId);

            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .setSelectedCard(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            for (Card card : cardList) {
                                card.setIsDefault(Const.FALSE);
                            }
                            cardList.get(selectedCardPosition).setIsDefault(Const.TRUE);
                            selectCardDataModify(selectedCardPosition);
                            if (isFromInvoice) {
                                onBackPressed();
                            }
                            Utils.hideCustomProgressDialog();
                        }
                    } else {
                        Utils.showErrorToast(response.body().getErrorCode(), PaymentActivity.this);
                        Utils.hideCustomProgressDialog();
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

    private void deleteCard(final int position) {
        Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_delete_card), false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            jsonObject.put(Const.Params.CARD_ID, cardList.get(position).getId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .deleteCard(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            boolean isSelectedCard =
                                    cardList.get(position).getIsDefault() == Const.TRUE;
                            cardList.remove(position);
                            cardAdapter.notifyItemRemoved(position);
                            updateUiCardList(!cardList.isEmpty());
                            Utils.hideCustomProgressDialog();
                            if (isSelectedCard && !cardList.isEmpty()) {
                                selectedCardPosition = 0;
                                selectCard(cardList.get(selectedCardPosition).getId());
                            }
                        } else {
                            Utils.hideCustomProgressDialog();
                            if (response.body().getErrorCode() == Const
                                    .ERROR_CODE_YOUR_TRIP_PAYMENT_IS_PENDING) {
                                openPendingPaymentDialog();
                            } else {
                                Utils.showErrorToast(response.body().getErrorCode(),
                                        PaymentActivity.this);
                            }
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

    private void selectCardDataModify(int position) {
        selectedCard = cardList.get(position);
        cardAdapter.notifyDataSetChanged();
    }

    private void openPendingPaymentDialog() {
        CustomDialogBigLabel pendingPayment = new CustomDialogBigLabel(this, getResources()
                .getString(R.string.text_payment_pending), getResources().getString(R.string
                .error_code_464), getResources().getString(R.string.text_email), getResources()
                .getString(R.string.text_cancel)) {
            @Override
            public void positiveButton() {
                dismiss();
                contactUsWithEmail(preferenceHelper.getContactUsEmail());
            }

            @Override
            public void negativeButton() {
                dismiss();
            }
        };
        pendingPayment.show();
    }

    private void addWalletAmount(double walletAmount, String paymentToken, String cardId) {
        Utils.showCustomProgressDialog(this, "", false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
            jsonObject.put(Const.Params.WALLET, walletAmount);
            jsonObject.put(Const.Params.CARD_ID, cardId);
            jsonObject.put(Const.Params.PAYMENT_TOKEN, paymentToken);
            jsonObject.put(Const.Params.USER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            Call<AddWalletResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .addWalletAmount(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<AddWalletResponse>() {
                @Override
                public void onResponse(Call<AddWalletResponse> call, Response<AddWalletResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Utils.hideCustomProgressDialog();
                            tvWalletAmount.setText(parseContent.twoDigitDecimalFormat.format
                                    (response
                                            .body().getWallet()) + " " +
                                    response.body().getWalletCurrencyCode());
                            updateUiForWallet(false);
                            Utils.showMessageToast(response.body().getMessage(), PaymentActivity
                                    .this);
                        } else {
                            Utils.hideCustomProgressDialog();
                            Utils.showErrorToast(response.body().getErrorCode(), PaymentActivity
                                    .this);
                        }
                    }

                }

                @Override
                public void onFailure(Call<AddWalletResponse> call, Throwable t) {
                    AppLog.handleThrowable(PaymentActivity.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.VIEW_AND_ADD_PAYMENT_ACTIVITY, e);
        }
    }

    private void updateUiForWallet(boolean isUpdate) {
        if (isUpdate) {
            //            llCreditCardList.setVisibility(View.GONE);
            llSubmitWalletAmount.setVisibility(View.VISIBLE);
            etWalletAmount.getText().clear();
            etWalletAmount.requestFocus();
            tvWalletAmount.setVisibility(View.GONE);
            tvSubmitWalletAmount.setText(R.string.text_submit);
        } else {
            llSubmitWalletAmount.setVisibility(View.GONE);
            tvWalletAmount.setVisibility(View.VISIBLE);
            tvSubmitWalletAmount.setText(R.string.text_topUP);
            //            llCreditCardList.setVisibility(View.VISIBLE);
        }

    }


    private void initPaymentGatewaySpinner() {
        paymentGatewaysList = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(this, paymentGatewaysList);
        spinnerPayment.setAdapter(paymentAdapter);
        spinnerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // do with item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do with view
            }
        });
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

    @Override
    public void onAdminApproved() {
        goWithAdminApproved();
    }

    @Override
    public void onAdminDeclined() {
        goWithAdminDecline();
    }

    @Override
    public void onprofileApprove() {

    }

    private void goToWalletHistoryActivity() {
        Intent intent = new Intent(this, WalletHistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    private void openDeleteCard(final int position) {
        CustomDialogBigLabel customDialogBigLabel = new CustomDialogBigLabel(this,
                getResources().getString(R.string.text_delete_card), getResources()
                .getString(R.string.msg_are_you_sure), getResources()
                .getString(R.string.text_ok), getResources()
                .getString(R.string.text_cancel)) {
            @Override
            public void positiveButton() {
                deleteCard(position);
                dismiss();
            }

            @Override
            public void negativeButton() {
                dismiss();
            }
        };
        customDialogBigLabel.show();

    }
}
