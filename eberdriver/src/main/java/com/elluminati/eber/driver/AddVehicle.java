package com.elluminati.eber.driver;


import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elluminati.eber.driver.adapter.VehicleSelectionAdapter;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyFontTextViewMedium;
import com.elluminati.eber.driver.fragments.MapFragment;
import com.elluminati.eber.driver.models.datamodels.CityType;
import com.elluminati.eber.driver.models.datamodels.Provider;
import com.elluminati.eber.driver.models.datamodels.TypeDetails;
import com.elluminati.eber.driver.models.datamodels.VehicleDetail;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.responsemodels.ProviderDetailResponse;
import com.elluminati.eber.driver.models.responsemodels.VehiclesResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elluminati.eber.driver.utils.Const.IMAGE_BASE_URL;

public class AddVehicle extends BaseAppCompatActivity {


    private ImageView ivCarImage;
    private String unit;
    private MyFontTextViewMedium tvCarName;
    private TextView tvCarType, tvCarPlateNo, tvCarBasePrice, tvCarDistancePrice,
            tvCarTimePrice;
    private MyFontTextView tvTagWallet;
    public CurrentTrip currentTrip;
    private MyFontButton btnAddVehicle;
    private RecyclerView rcvSelectVehicle;
    private ArrayList<VehicleDetail> listVehicle;
    private VehicleSelectionAdapter vehicleSelectionAdapter;
    private NumberFormat currencyFormat;
    private LinearLayout llVehicleDetail;
    private CheckBox betCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        initToolBar();
        setTitleOnToolbar(getString(R.string.text_your_vehicle));

        ivCarImage = (ImageView) findViewById(R.id.ivCarImage);
        tvTagWallet = findViewById(R.id.tvTagWallet);
        tvCarName = (MyFontTextViewMedium) findViewById(R.id.tvCarName);
        tvCarPlateNo = findViewById(R.id.tvCarPlateNo);
        tvCarBasePrice = findViewById(R.id.tvCarBasePrice);
        tvCarDistancePrice = findViewById(R.id.tvCarDistancePrice);
        tvCarTimePrice = findViewById(R.id.tvCarTimePrice);
        tvCarType = findViewById(R.id.tvCarType);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        rcvSelectVehicle = findViewById(R.id.rcvSelectVehicle);
        llVehicleDetail =findViewById(R.id.llVehicleDetail);

        betCheck = findViewById(R.id.betCheck);



        betCheck.setChecked(preferenceHelper.getCheckEnd());
        betCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    //   Toast.makeText(drawerActivity, "true", Toast.LENGTH_SHORT).show();
                    getisChecked(isChecked);
                } else {
                    // Toast.makeText(drawerActivity, "fals", Toast.LENGTH_SHORT).show();
                    getisChecked(isChecked);
                }

            }
        });

        currentTrip = CurrentTrip.getInstance();

        currencyFormat =
                currencyHelper.getCurrencyFormat(preferenceHelper.getCurrencyCode());
        setToolbarBackgroundAndElevation(true, R.drawable
                .toolbar_bg_rounded_bottom, R
                .dimen.toolbar_elevation);
//        setTitleOnToolbarMap("Hi," + " " + preferenceHelper.getFirstName() + " !", preferenceHelper.getProfilePic());


        getProviderDetail();
        initVehicleBottomSheet();
        btnAddVehicle.setOnClickListener(this);


        initVehicleList();

    }

    private void getisChecked(boolean isCheck) {

        Utils.showCustomProgressDialog(AddVehicle.this, "", false, null);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.ISENDOFTRIP, isCheck);

            Call<ProviderDetailResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getProviderdata(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<ProviderDetailResponse>() {
                @Override
                public void onResponse(Call<ProviderDetailResponse> call, Response<ProviderDetailResponse>
                        response) {

                    if (response.body().isSuccess()) {

                        Provider provider = response.body().getProvider();


                        if (provider.getLatlngEND() != null) {

                            preferenceHelper.putaddressEndtrip(provider.getEndTripAddress());

                            preferenceHelper.putLatEndtrip(provider.getLatlngEND().get(0));

                            preferenceHelper.putLngEndtrip(provider.getLatlngEND().get(1));

                            preferenceHelper.putCheckEnd(provider.getCheckEndTrip());

                        } else {
                            preferenceHelper.putaddressEndtrip(getResources().getString(R.string.setAddressstring));
                            preferenceHelper.putCheckEnd(false);
                            Toast.makeText(AddVehicle.this, "Please Select End Of The Day Trip Address", Toast.LENGTH_SHORT).show();
                        }

                    }

                    betCheck.setChecked(preferenceHelper.getCheckEnd());

                    Utils.hideCustomProgressDialog();


                    Utils.hideCustomProgressDialog();


                }

                @Override
                public void onFailure(Call<ProviderDetailResponse> call, Throwable t) {
                    AppLog.handleThrowable(MapFragment.class.getSimpleName(), t);
                }
            });


        } catch (JSONException e) {
            AppLog.handleException("GetProviderVehicleList", e);
        }
    }


    private void initVehicleList() {
        listVehicle = new ArrayList<>();
        rcvSelectVehicle.setLayoutManager(new LinearLayoutManager(AddVehicle.this));
        vehicleSelectionAdapter = new VehicleSelectionAdapter(AddVehicle.this, listVehicle) {
            @Override
            public void onVehicleSelect(int position, String vehicleId, boolean
                    isHaveServiceTypeID) {
                if (!listVehicle.get(position).isIsSelected()) {
                    if (!isHaveServiceTypeID) {
                        Utils.showToast(getResources().getString(R.string.message_vehicle_not_approved),
                                AddVehicle.this);
                    } else {
                        changeCurrentVehicle(listVehicle.get(position).getId());
                        vehicleSelectionAdapter.changeSelection(position);
                    }
                }
            }

            @Override
            public void onVehicleClick(String vehicleId) {
                goToAddVehicleDetailActivity(false, vehicleId);
            }
        };
        rcvSelectVehicle.setAdapter(vehicleSelectionAdapter);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnAddVehicle:
                goToAddVehicleDetailActivity(true, null);
                break;
            default:
                // do with default
                break;
        }

    }


    private void getProviderDetail() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.APP_VERSION, getAppVersion());
            Call<ProviderDetailResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getProviderDetail(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<ProviderDetailResponse>() {
                @Override
                public void onResponse(Call<ProviderDetailResponse> call,
                                       Response<ProviderDetailResponse> response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        if (response.body().isSuccess()) {

                            preferenceHelper.putIdPendingProfile(response.body().getPendingprofileupdate());
                            Provider provider = response.body().getProvider();
                            currencyFormat =
                                    currencyHelper.getCurrencyFormat(preferenceHelper.getCurrencyCode());
                            currentTrip.getSpeakingLanguages().clear();
                            currentTrip.setSpeakingLanguages(
                                    (ArrayList<String>) provider
                                            .getLanguages());
                            preferenceHelper.putProviderType(provider
                                    .getProviderType());
                            preferenceHelper.putRating(String.valueOf
                                    (provider
                                            .getRate
                                                    ()));
                            preferenceHelper.putIsProviderOnline(provider
                                    .getIsActive());
                            //firstname


                            if (provider.getLatlngEND() != null) {

                                preferenceHelper.putLatEndtrip(provider.getLatlngEND().get(0));

                                preferenceHelper.putLngEndtrip(provider.getLatlngEND().get(1));

                                preferenceHelper.putaddressEndtrip(provider.getEndTripAddress());

                                preferenceHelper.putCheckEnd(provider.getCheckEndTrip());

                                betCheck.setChecked(preferenceHelper.getCheckEnd());

                            } else {
                                preferenceHelper.putaddressEndtrip(getString(R.string.setAddressstring));

                            }


                            preferenceHelper.putFirstName(provider
                                    .getFirstName());
                            preferenceHelper.putLastName(provider
                                    .getLastName());
                            preferenceHelper.putEmail(provider
                                    .getEmail());
                            preferenceHelper.putContact(provider
                                    .getPhone());
                            preferenceHelper.putProfilePic(IMAGE_BASE_URL + provider.getPicture());

                            preferenceHelper.putIsApproved(provider
                                    .getIsApproved());
                            preferenceHelper.putIsPartnerApprovedByAdmin
                                    (provider.getIsPartnerApprovedByAdmin());
                            preferenceHelper.putIsDocumentExpire(provider
                                    .isIsDocumentsExpired());
                            preferenceHelper.putProviderType(provider
                                    .getProviderType());
                            if (vehicleSelectionAdapter != null) {
                                vehicleSelectionAdapter.setVehicleChangeEnable(preferenceHelper.getProviderType() != Const.ProviderStatus.PROVIDER_TYPE_PARTNER);

                            }
                            currentTrip.setWalletCurrencyCode(provider
                                    .getWalletCurrencyCode());
                            if (provider.isIsDocumentsExpired()) {
                                updateUIForAdminApproved(false, Const.Pending
                                        .DOCUMENT_EXPIRE);
                            } else if (provider.getVehicleDetail()
                                    == null || provider.getVehicleDetail()
                                    .isEmpty()) {
                                updateUIForAdminApproved(false, Const.Pending
                                        .ADD_VEHICLE);
                            } else {
                                for (VehicleDetail vehicleDetail :
                                        provider.getVehicleDetail()) {
                                    if (vehicleDetail.isIsSelected()) {
                                        currentTrip.setCurrentVehicle(vehicleDetail);
                                        break;
                                    } else {
                                        currentTrip.setCurrentVehicle(null);
                                    }

                                }
                                if (currentTrip.getCurrentVehicle() != null) {
                                    String vehicleName = currentTrip
                                            .getCurrentVehicle().getName() + " " + currentTrip.getCurrentVehicle().getModel();
                                    tvCarName.setText(vehicleName);
                                    tvCarPlateNo.setText(currentTrip
                                            .getCurrentVehicle().getPlateNo());
                                    TypeDetails typeDetails = response.body().getTypeDetails();

                                  /*  if (typeDetails.isAutoTransfer()) {
                                        drawerAdapter.clearDrawerNotification
                                                (getResources()
                                                        .getString(R.string.text_bank_detail));
                                    } else {
                                        drawerAdapter.setDrawerNotification
                                                (drawerActivity.getResources()
                                                        .getString(R.string.text_bank_detail));
                                    }*/
                                    if (typeDetails
                                            .isCheckProviderWalletAmountForReceivedCashRequest()
                                            && provider.getWallet() < typeDetails
                                            .getProviderMinWalletAmountSetForReceivedCashRequest
                                                    ()) {
                                        String msg =
                                                getResources().getString(R.string
                                                        .msg_min_wallet_required) + " " + currencyFormat.format(
                                                        typeDetails
                                                                .getProviderMinWalletAmountSetForReceivedCashRequest());
                                        tvTagWallet.setText(msg);
                                        tvTagWallet.setVisibility(View.VISIBLE);
                                    } else {
                                        tvTagWallet.setVisibility(View.GONE);
                                    }
                                    driverRemainInfoShouldUpdate();
                                    unit = Utils.showUnit(AddVehicle.this,
                                            typeDetails.getUnit());
                                    ivCarImage.setVisibility(View.VISIBLE);
                                    GlideApp.with(getApplicationContext()).load(IMAGE_BASE_URL
                                            + typeDetails.getTypeImageUrl())
                                            .fallback(R.drawable.ellipse)
                                            .override(200, 200).dontAnimate()
                                            .into(ivCarImage);

                                    tvCarBasePrice.setText(currencyFormat.format(typeDetails
                                            .getBasePrice())
                                            + Utils
                                            .validBasePriceSuffix
                                                    (typeDetails.getBasePriceDistance(), unit));
                                    tvCarDistancePrice.setText(currencyFormat.format(typeDetails
                                            .getDistancePrice()) + Const.SLASH
                                            + unit);
                                    tvCarTimePrice.setText(currencyFormat.format(typeDetails
                                            .getTimePrice()) +
                                            getResources()
                                                    .getString(R.string
                                                            .text_unit_per_min));
                                    tvCarType.setText(typeDetails.getTypename());

                                } else {
                                   /* updateUIForAdminApproved(false, Const.Pending
                                            .PENDING_FOR_ADMIN_APPROVAL);*/
                                }

                            }

                        }
                    }
                }

                @Override
                public void onFailure(Call<ProviderDetailResponse> call, Throwable t) {
                    AppLog.handleThrowable(MapFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }


    private boolean updateUIForAdminApproved(boolean isApproved, int pending) {
        //updateUIVehicleDetail(isApproved);

        if (isApproved) {

            llVehicleDetail.setVisibility(View.VISIBLE);

        } else {

            llVehicleDetail.setVisibility(View.GONE);

        }
        return isApproved;
    }

    private void initVehicleBottomSheet() {

        listVehicle = new ArrayList<>();


        if (listVehicle.isEmpty()) {
            getProviderVehicleList();
        } else {
            driverRemainInfoShouldUpdate();
        }


    }

    private void getProviderVehicleList() {
        Utils.showCustomProgressDialog(AddVehicle.this, "", false, null);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Const.Params.PROVIDER_ID, AddVehicle.this
                    .preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN,
                    preferenceHelper.getSessionToken());

            Call<VehiclesResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getVehicles(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<VehiclesResponse>() {
                @Override
                public void onResponse(Call<VehiclesResponse> call, Response<VehiclesResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {

                        if (response.body().isSuccess()) {
                            listVehicle.clear();
                            listVehicle.addAll(response.body().getVehicleList());
                            vehicleSelectionAdapter.notifyDataSetChanged();
                            driverRemainInfoShouldUpdate();
                        }

                        Utils.hideCustomProgressDialog();
                    }

                }

                @Override
                public void onFailure(Call<VehiclesResponse> call, Throwable t) {
                    AppLog.handleThrowable(MapFragment.class.getSimpleName(), t);
                }
            });


        } catch (JSONException e) {
            AppLog.handleException("GetProviderVehicleList", e);
        }
    }

    private void changeCurrentVehicle(String vehicleId) {
        Utils.showCustomProgressDialog(AddVehicle.this, "", false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.TOKEN,
                    preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.VEHICLE_ID, vehicleId);


            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .changeCurrentVehicle(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        getProviderDetail();

                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    Utils.hideCustomProgressDialog();
                    AppLog.handleThrowable(MapFragment.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException("change vehicle", e);
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
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {

    }


    /**
     * Go to add vehicle detail activity.
     *
     * @param isAddVehicle the is add vehicle
     * @param vehicleId    the vehicle id
     */
    public void goToAddVehicleDetailActivity(boolean isAddVehicle, String vehicleId) {
        Intent intent = new Intent(AddVehicle.this, AddVehicleDetailActivity.class);
        intent.putExtra(Const.IS_ADD_VEHICLE, isAddVehicle);
        intent.putExtra(Const.VEHICLE_ID, vehicleId);
        startActivityForResult(intent, Const.REQUEST_ADD_VEHICLE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_OK);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_ADD_VEHICLE) {
            getProviderVehicleList();
        }

    }


    private void driverRemainInfoShouldUpdate() {
        boolean isRequiredUpdate = false;
        for (VehicleDetail vehicleDetail : listVehicle) {
            if (vehicleDetail.isIsSelected()) {
                if (vehicleDetail.isIsDocumentsExpired()) {
                    isRequiredUpdate = true;
                    break;
                }
            }
        }
        if (isRequiredUpdate) {
            updateUiForDriverRemainInfo(isRequiredUpdate);
        } else {
            updateUiForDriverRemainInfo(tvTagWallet.getVisibility() == View.VISIBLE);
        }


    }

    /**
     * alert to driver for required info is remain to update
     *
     * @param isRequiredUpdate is if its required
     */
    private void updateUiForDriverRemainInfo(boolean isRequiredUpdate) {
      /*  if (isRequiredUpdate) {
            tvCarName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    AppCompatResources.getDrawable(AddVehicle.this, R.drawable.info_red_icon),
                    null);
        } else {
            tvCarName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    null,
                    null);
        }*/
    }
}
