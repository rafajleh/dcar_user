package com.elluminati.eber.driver;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.elluminati.eber.driver.adapter.SpeakingLanguageAdaptor;
import com.elluminati.eber.driver.components.CustomLanguageDialog;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.models.datamodels.Language;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.responsemodels.LanguageResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseAppCompatActivity implements CompoundButton
        .OnCheckedChangeListener {

    SwitchCompat switchRequestAlert, switchPickUpAlert, switchPushNotificationSound, switchHeatMap;
    private MyFontTextView tvVersion, tvLanguage;
    private CustomLanguageDialog customLanguageDialog;
    private RecyclerView rcvSpeakingLanguage;
    private ArrayList<Language> languages = new ArrayList<>();
    private CheckBox cbMale, cbFemale;
    private LinearLayout llNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_settings));
        tvVersion = (MyFontTextView) findViewById(R.id.tvVersion);
        switchRequestAlert = (SwitchCompat) findViewById(R.id.switchRequestSound);
        switchPickUpAlert = (SwitchCompat) findViewById(R.id.switchPickUpSound);
        switchPushNotificationSound = (SwitchCompat) findViewById(R.id.switchPushNotificationSound);
        tvLanguage = (MyFontTextView) findViewById(R.id.tvLanguage);
        switchHeatMap = (SwitchCompat) findViewById(R.id.switchHeatMap);
        rcvSpeakingLanguage = findViewById(R.id.rcvSpeakingLanguage);
        llNotification=findViewById(R.id.llNotification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            llNotification.setVisibility(View.GONE);
        } else {
            llNotification.setVisibility(View.VISIBLE);
        }
        switchPushNotificationSound.setOnCheckedChangeListener(this);
        switchRequestAlert.setOnCheckedChangeListener(this);
        switchPickUpAlert.setOnCheckedChangeListener(this);
        switchHeatMap.setOnCheckedChangeListener(this);
        tvLanguage.setOnClickListener(this);
        switchPushNotificationSound.setChecked(preferenceHelper.getIsPushNotificationSoundOn());
        switchRequestAlert.setChecked(preferenceHelper.getIsSoundOn());
        switchPickUpAlert.setChecked(preferenceHelper.getIsPickUpSoundOn());
        switchHeatMap.setChecked(preferenceHelper.getIsHeatMapOn());
        tvVersion.setText(getAppVersion());
        cbMale = findViewById(R.id.cbMale);
        cbFemale = findViewById(R.id.cbFemale);
        setLanguageName();
        getSpeakingLanguages();
        setToolbarRightSideButton(getResources().getString(R.string
                .text_save), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvLanguage:
                openLanguageDialog();
                break;
            case R.id.btnToolBar:
                upDateSettings();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchRequestSound:
                if (isChecked) {
                    preferenceHelper.putIsSoundOn(true);
                } else {
                    preferenceHelper.putIsSoundOn(false);
                }
                break;
            case R.id.switchPickUpSound:
                if (isChecked) {
                    preferenceHelper.putIsPickUpSoundOn(true);
                } else {
                    preferenceHelper.putIsPickUpSoundOn(false);
                }
                break;
            case R.id.switchPushNotificationSound:
                if (isChecked) {
                    preferenceHelper.putIsPushNotificationSoundOn(true);
                } else {
                    preferenceHelper.putIsPushNotificationSoundOn(false);
                }
                break;
            case R.id.switchHeatMap:
                if (isChecked) {
                    preferenceHelper.putIsHeatMapOn(true);
                } else {
                    preferenceHelper.putIsHeatMapOn(false);
                }
                break;
            default:
                // do with default
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private void openLanguageDialog() {
        if (customLanguageDialog != null && customLanguageDialog.isShowing()) {
            return;
        }
        customLanguageDialog = new CustomLanguageDialog(this) {
            @Override
            public void onSelect(String languageName, String languageCode) {
                tvLanguage.setText(languageName);
                if (!TextUtils.equals(preferenceHelper.getLanguageCode(),
                        languageCode)) {
                    preferenceHelper.putLanguageCode(languageCode);
                    finishAffinity();
                    restartApp();
                }
                dismiss();
            }
        };
        customLanguageDialog.show();
    }

    private void setLanguageName() {
        TypedArray array = getResources().obtainTypedArray(R.array.language_code);
        TypedArray array2 = getResources().obtainTypedArray(R.array.language_name);
        int size = array.length();
        for (int i = 0; i < size; i++) {
            if (TextUtils.equals(preferenceHelper.getLanguageCode(), array.getString(i))) {
                tvLanguage.setText(array2.getString(i));
                break;
            }
        }

    }


    private void getSpeakingLanguages() {
        JSONObject jsonObject = new JSONObject();
        Call<LanguageResponse> call = ApiClient.getClient().create(ApiInterface.class)
                .getLanguageForTrip(ApiClient.makeJSONRequestBody(jsonObject));
        call.enqueue(new Callback<LanguageResponse>() {
            @Override
            public void onResponse(Call<LanguageResponse> call, Response<LanguageResponse>
                    response) {
                if (ParseContent.getInstance().isSuccessful(response)) {
                    if (response.body().isSuccess()) {
                        for (int i = 0; i < CurrentTrip.getInstance().getSpeakingLanguages().size
                                (); i++) {
                            List<Language> languages = response.body().getLanguages();
                            for (int j = 0; j < languages.size(); j++) {
                                if (TextUtils.equals(CurrentTrip.getInstance()
                                                .getSpeakingLanguages().get(i),
                                        languages.get(j).getId())) {
                                    languages.get(j).setSelected(true);
                                }
                            }
                        }
                        languages.addAll(response.body().getLanguages());
                        rcvSpeakingLanguage.setLayoutManager(new LinearLayoutManager
                                (SettingActivity.this));
                        rcvSpeakingLanguage.setAdapter(new SpeakingLanguageAdaptor
                                (SettingActivity.this,
                                        languages));
                        rcvSpeakingLanguage.setNestedScrollingEnabled(false);
                    } else {
                        Utils.showErrorToast(response.body().getErrorCode(), SettingActivity.this);
                    }
                }


            }

            @Override
            public void onFailure(Call<LanguageResponse> call, Throwable t) {
                AppLog.handleThrowable(SettingActivity.class.getSimpleName(), t);
            }
        });
    }


    private void upDateSettings() {

        JSONObject jsonObject = new JSONObject();
        JSONArray lanJsonArray = new JSONArray();
        //  JSONArray genderJsonArray = new JSONArray();
        try {
            /*if (cbMale.isChecked()) {
                genderJsonArray.put(Const.Gender.MALE);
            }

            if (cbFemale.isChecked()) {
                genderJsonArray.put(Const.Gender.FEMALE);
            }*/

            /*if (genderJsonArray.length() == 0) {
                Utils.showToast(getResources().getString(R.string.msg_plz_select_at_one_user_type),
                        this);
                return;
            }*/

            for (Language language : languages) {
                if (language.isSelected()) {
                    lanJsonArray.put(language.getId());
                }

            }
            if (lanJsonArray.length() == 0) {
                Utils.showToast(getResources().getString(R.string.msg_plz_select_at_one_language),
                        this);
                return;
            }

            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.LANGUAGES, lanJsonArray);
            // jsonObject.put(Const.Params.RECEIVED_TRIP_FROM_GENDER, genderJsonArray);
            Utils.showCustomProgressDialog(this, getResources().getString(R.string
                    .msg_waiting_for_update_profile), false, null);
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .updateProviderSetting(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    Utils.hideCustomProgressDialog();
                    if (response.body().isSuccess()) {
                        onBackPressed();
                    } else {
                        Utils.showErrorToast(response.body().getErrorCode(), SettingActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(SettingActivity.class.getSimpleName(), t);
                }
            });

        } catch (Exception e) {
            AppLog.handleException(Const.Tag.PROFILE_ACTIVITY, e);
        }
    }

    private void setGenderWiseRequests() {
        for (String gender : CurrentTrip.getInstance().getGenderWiseRequests()) {
            if (TextUtils.equals(Const.Gender.MALE, gender)) {
                cbMale.setChecked(true);
            }
            if (TextUtils.equals(Const.Gender.FEMALE, gender)) {
                cbFemale.setChecked(true);
            }
        }
    }

}
