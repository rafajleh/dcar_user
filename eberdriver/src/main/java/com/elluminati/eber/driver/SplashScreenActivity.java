package com.elluminati.eber.driver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.models.datamodels.AdminSettings;
import com.elluminati.eber.driver.models.responsemodels.SettingsDetailsResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends BaseAppCompatActivity {


    int oneTimeCall;
    ImageView imglogo;
    LinearLayout lltxtview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        imglogo=findViewById(R.id.imglogo);
        lltxtview=findViewById(R.id.lltxtview);

        Animation animation= AnimationUtils.loadAnimation(SplashScreenActivity.this,R.anim.zoomin);
        imglogo.startAnimation(animation);
        lltxtview.startAnimation(animation);

        Handler mHandler = new Handler(getMainLooper());
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                checkIfGpsOrInternetIsEnable();
            }
        };
        mHandler.postDelayed(mRunnable, 2500);

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
        // do somethings

    }


    @Override
    public void onClick(View v) {
        // do somethings
    }

    private void checkIfGpsOrInternetIsEnable() {
        if (!Utils.isInternetConnected(this)) {
            openInternetDialog();
        } else {
            closedEnableDialogGps();
            closedEnableDialogInternet();
            getAPIKeys();
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogInternet();
            getAPIKeys();
        } else {
            openInternetDialog();
        }
    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogGps();
            getAPIKeys();
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


    private void openUpdateAppDialog(final boolean isForceUpdate) {
        String btnNegative;
        if (isForceUpdate) {
            btnNegative = getResources()
                    .getString(R.string.text_exit_caps);
        } else {
            btnNegative =
                    getResources()
                            .getString(R.string.text_not_now);
        }

        final CustomDialogBigLabel customDialogBigLabel = new CustomDialogBigLabel(this,
                getResources()
                        .getString(R.string.text_update_app), getResources()
                .getString(R.string.meg_update_app), getResources()
                .getString(R.string.text_update), btnNegative) {
            @Override
            public void positiveButton() {
                final String appPackageName = getPackageName(); // getPackageName() from Context
                // or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                            + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" +
                            ".com/store/apps/details?id=" + appPackageName)));
                }
                dismiss();
                finishAffinity();
            }

            @Override
            public void negativeButton() {
                dismiss();
                if (isForceUpdate) {
                    finishAffinity();
                } else {
                    if (!TextUtils.isEmpty(preferenceHelper.getSessionToken())) {
                        goToMainDrawerActivity();
                    } else {
                        goToMainActivity();
                    }
                }


            }
        };
        if (!isFinishing()) {
            customDialogBigLabel.show();
        }


    }

    private boolean checkVersionCode(String code) {

        String[] appVersionWeb = code
                .split("\\.");
        String[] appVersion = getAppVersion().split("\\.");

        int sizeWeb = appVersionWeb.length;
        int sizeApp = appVersion.length;
        if (sizeWeb == sizeApp) {
            for (int i = 0; i < sizeWeb; i++) {
                if (Double.valueOf(appVersionWeb[i]) > Double
                        .valueOf(appVersion[i])) {
                    return true;
                } else if ((Integer.valueOf(appVersionWeb[i]) == Integer
                        .valueOf(appVersion[i]))) {
                } else {
                    return false;
                }

            }
            return false;
        }
        return true;

    }

    private void getAPIKeys() {
        if (oneTimeCall == 0) {
            oneTimeCall++;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.PROVIDER_ID, !TextUtils.isEmpty(preferenceHelper
                        .getSessionToken()) ? preferenceHelper.getProviderId() : null);
                jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
                jsonObject.put(Const.Params.APP_VERSION, getAppVersion());
                jsonObject.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
                Call<SettingsDetailsResponse> call = ApiClient.getClient().create(ApiInterface
                        .class)
                        .getProviderSettingDetail
                                (ApiClient.makeJSONRequestBody(jsonObject));
                call.enqueue(new Callback<SettingsDetailsResponse>() {
                    @Override
                    public void onResponse(Call<SettingsDetailsResponse> call,
                                           Response<SettingsDetailsResponse>
                                                   response) {

                        if (parseContent.isSuccessful(response)) {
                            parseContent.parseProviderSettingDetail(response.body());

                            /***
                             * check if user login session is not expired
                             */
                            AdminSettings adminSettings = response.body().getAdminSettings();
                            if (checkVersionCode(adminSettings.getAndroidProviderAppVersionCode
                                    ())) {
                                openUpdateAppDialog(adminSettings
                                        .isAndroidProviderAppForceUpdate());
                            } else {


                                if (TextUtils.isEmpty(preferenceHelper.getSessionToken())) {
                                    goToMainActivity();
                                } else {
                                    moveWithUserSpecificPreference();
                                }


                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<SettingsDetailsResponse> call, Throwable t) {
                        AppLog.handleThrowable(BaseAppCompatActivity.class.getSimpleName(), t);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
