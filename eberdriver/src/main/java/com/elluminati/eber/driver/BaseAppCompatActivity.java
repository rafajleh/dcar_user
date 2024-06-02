package com.elluminati.eber.driver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.CustomDialogEnable;
import com.elluminati.eber.driver.components.MyAppTitleFontTextView;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.interfaces.AdminApprovedListener;
import com.elluminati.eber.driver.interfaces.ConnectivityReceiverListener;
import com.elluminati.eber.driver.models.datamodels.Provider;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.responsemodels.ProviderDetailResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.CurrencyHelper;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.LanguageHelper;
import com.elluminati.eber.driver.utils.NetworkHelper;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elluminati.eber.driver.utils.Const.IMAGE_BASE_URL;
import static com.elluminati.eber.driver.utils.Utils.isScreenOn;

/**
 * Created by elluminati on 10-05-2016.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements View
        .OnClickListener, ConnectivityReceiverListener,
        AdminApprovedListener {

    public TextView tvTitle;
    public MyAppTitleFontTextView tvTimeRemain;
    public PreferenceHelper preferenceHelper;
    public ParseContent parseContent;
    public String TAG = this.getClass().getSimpleName();
    protected Toolbar toolbar;
    protected ActionBar actionBar;
    private MyFontButton btnToolBar;
    private ImageView ivToolbarIcon,profileicon;
    private CustomDialogEnable customDialogEnableGps, customDialogEnableInternet;
    private CustomDialogBigLabel customDialogExit, customDialogUnderReview;
    private ConnectivityReceiverListener connectivityReceiverListener;
    private AdminApprovedListener adminApprovedListener;
    private AppReceiver appReceiver = new AppReceiver();
    public CurrencyHelper currencyHelper;
    private NetworkHelper networkHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(ResourcesCompat.getColor(getResources(), R.color
                        .color_app_status_bar_green,
                null));
        preferenceHelper = PreferenceHelper.getInstance(this);
        parseContent = ParseContent.getInstance();
        currencyHelper = CurrencyHelper.getInstance(this);
        parseContent.getContext(this);
        adjustFontScale(getResources().getConfiguration());
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Const.GPS_ACTION);
        intentFilter.addAction(Const.ACTION_DECLINE_PROVIDER);
        intentFilter.addAction(Const.ACTION_APPROVED_PROVIDER);
        intentFilter.addAction(Const.PROFILE_APPROVED_ACTION);
        intentFilter.addAction(Const.ACTION_NEW_TRIP);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build
                .VERSION_CODES.LOLLIPOP) {
            networkHelper = NetworkHelper.getInstance();
            networkHelper.initConnectivityManager(this);
        } else {
            intentFilter.addAction(Const.NETWORK_ACTION);
        }
        registerReceiver(appReceiver, intentFilter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    protected void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.myToolbar);
        tvTitle = toolbar.findViewById(R.id.tvTitle);
        ivToolbarIcon = (ImageView) toolbar.findViewById(R.id.ivToolbarIcon);
        profileicon=toolbar.findViewById(R.id.profileicon);
        tvTimeRemain = (MyAppTitleFontTextView) toolbar.findViewById(R.id.tvTimeRemain);
        btnToolBar = (MyFontButton) toolbar.findViewById(R.id.btnToolBar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goWithBackArrow();
            }
        });
    }

    public void setTitleOnToolbar(String title) {
        tvTitle.setText(title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTimeRemain.setVisibility(View.GONE);
        ivToolbarIcon.setVisibility(View.GONE);
        btnToolBar.setVisibility(View.GONE);
    }

    public void setTitleOnToolbarMap(String title,String profile) {

        tvTitle.setText(title);
        GlideApp.with(this).load(profile)
                .dontAnimate().placeholder(R.drawable.profile1).override(120, 120).into
                (profileicon);

        tvTitle.setVisibility(View.VISIBLE);
        profileicon.setVisibility(View.VISIBLE);

        tvTimeRemain.setVisibility(View.GONE);
        ivToolbarIcon.setVisibility(View.GONE);
        btnToolBar.setVisibility(View.GONE);
    }

    public void setToolbarIcon(Drawable drawable, View.OnClickListener onClickListener) {
        ivToolbarIcon.setImageDrawable(drawable);
        profileicon.setVisibility(View.GONE);
        ivToolbarIcon.setOnClickListener(onClickListener);
        ivToolbarIcon.setVisibility(View.VISIBLE);
    }

    public void setToolbarRightSideButton(String title, View.OnClickListener onClickListener) {
        ivToolbarIcon.setVisibility(View.GONE);
        tvTimeRemain.setVisibility(View.GONE);
        profileicon.setVisibility(View.GONE);
        btnToolBar.setVisibility(View.VISIBLE);
        btnToolBar.setOnClickListener(onClickListener);
        btnToolBar.setText(title);
    }

    public void hideToolbarRightSideButton(boolean isHide) {
        if (ivToolbarIcon != null) {
            ivToolbarIcon.setVisibility(isHide ? View.GONE : View.VISIBLE);
        }

    }

    public void setToolbarBackgroundAndElevation(boolean isDrawable, int resId, int elevationId) {
        if (toolbar != null) {
            if (isDrawable) {
                toolbar.setBackground(AppCompatResources.getDrawable(this, resId));
            } else {
                toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), resId, null));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (elevationId <= 0) {
                    toolbar.setElevation(0);
                } else {
                    toolbar.setElevation(getResources().getDimensionPixelOffset(elevationId));
                }


            }
        }
    }

    public void hideToolbarButton() {
        btnToolBar.setVisibility(View.GONE);
    }

    protected void openExitDialog(final Activity activity) {

        customDialogExit = new CustomDialogBigLabel(this, getString(R
                .string.text_exit_caps), getString(R.string.msg_are_you_sure), getString(R.string
                .text_yes), getString(R
                .string.text_no)) {
            @Override
            public void positiveButton() {
                customDialogExit.dismiss();
                activity.finishAffinity();
            }

            @Override
            public void negativeButton() {
                customDialogExit.dismiss();
            }
        };
        customDialogExit.show();
    }

    protected void openGpsDialog() {
        if (customDialogEnableGps != null && customDialogEnableGps.isShowing()) {
            return;
        }
        customDialogEnableGps = new CustomDialogEnable(this, getString(R.string.msg_gps_enable),
                getString(R.string.text_no), getString(R.string.text_yes)) {
            @Override
            public void doWithEnable() {
                startActivityForResult(new Intent(Settings
                        .ACTION_LOCATION_SOURCE_SETTINGS), Const.ACTION_SETTINGS);
            }

            @Override
            public void doWithDisable() {
                closedEnableDialogGps();
                finishAffinity();
            }
        };

       /* if(!this.isFinishing())
            customDialogEnableGps.show();*/
    }


    protected void openInternetDialog() {
        if (customDialogEnableInternet != null && customDialogEnableInternet.isShowing()) {
            return;
        }

        customDialogEnableInternet = new CustomDialogEnable(this, getString(R.string
                .msg_internet_enable), getString(R.string.text_no), getString(R.string.text_yes)) {
            @Override
            public void doWithEnable() {
                startActivityForResult(new Intent(Settings
                        .ACTION_SETTINGS), Const.ACTION_SETTINGS);
            }

            @Override
            public void doWithDisable() {
                closedEnableDialogInternet();
                finishAffinity();
            }
        };

        if (!this.isFinishing())
            customDialogEnableInternet.show();

    }

    protected void closedEnableDialogGps() {
        if (customDialogEnableGps != null && customDialogEnableGps.isShowing()) {
            customDialogEnableGps.dismiss();
            customDialogEnableGps = null;

        }
    }

    protected void closedEnableDialogInternet() {
        if (customDialogEnableInternet != null && customDialogEnableInternet.isShowing()) {
            customDialogEnableInternet.dismiss();
            customDialogEnableInternet = null;

        }
    }


    protected void goToDocumentActivity(boolean isClickInsideDrawer) {
        Intent docIntent = new Intent(this, DocumentActivity.class);
        docIntent.putExtra(Const.IS_CLICK_INSIDE_DRAWER, isClickInsideDrawer);
        startActivity(docIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void goToReferralActivity() {
        Intent docIntent = new Intent(this, RefferralApplyActivity.class);
        startActivity(docIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void goToAddcardActivity() {
        Intent docIntent = new Intent(this, AddCardActivity.class);
        docIntent.putExtra("registerfee","registerfee");
        startActivity(docIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void goToShowReferralActivity() {
        Intent docIntent = new Intent(this, ShowReferralActivity.class);
        startActivity(docIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void goToMainDrawerActivity() {
        Intent mainDrawerIntent = new Intent(this, MainDrawerActivity
                .class);
        mainDrawerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainDrawerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainDrawerIntent);
        overridePendingTransition(R.anim.fade_in_fast, R.anim.fade_out_fast);
    }

    protected void goToMainActivity() {
        Intent sigInIntent = new Intent(this, MainActivity.class);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sigInIntent);
        finishAffinity();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void goToRegisterActivity() {
        Intent registerIntent = new Intent(this, Register_Step_Input.class);
        startActivity(registerIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void goToSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public void goToAddVehicleDetailActivity(boolean isAddVehicle, String vehicleId) {
        Intent intent = new Intent(this, AddVehicleDetailActivity.class);
        intent.putExtra(Const.IS_ADD_VEHICLE, isAddVehicle);
        intent.putExtra(Const.VEHICLE_ID, vehicleId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected abstract boolean isValidate();


    /**
     * @param email
     * @param vehicleId
     * @deprecated
     */
    public void openUnderReviewDialog(final String email, final String vehicleId) {

        if (customDialogUnderReview != null && customDialogUnderReview.isShowing()) {
            return;
        }

        final String positiveButtonTitle;
        final String message;

        if (TextUtils.equals(email, Const.ADD_VEHICLE)) {
            if (TextUtils.isEmpty(vehicleId))
                positiveButtonTitle = getString(R.string.text_add_vehicle);
            else
                positiveButtonTitle = getString(R.string.text_edit_vehicle);

            message = getString(R.string.message_add_vehicle);
        } else {
            positiveButtonTitle = getString(R.string.text_email);
            message = getString(R.string.msg_under_review);
        }

        customDialogUnderReview = new CustomDialogBigLabel(this, getString(R.string
                .text_admin_alert), message, positiveButtonTitle, getString(R.string.text_logout)) {
            @Override
            public void positiveButton() {
                if (TextUtils.equals(positiveButtonTitle, getString(R.string.text_add_vehicle))
                        || TextUtils.equals(positiveButtonTitle, getString(R.string
                        .text_edit_vehicle))) {
                    closedUnderReviewDialog();
                    if (TextUtils.isEmpty(vehicleId)) {
                        goToAddVehicleDetailActivity(true, null);
                    } else {
                        goToAddVehicleDetailActivity(false, vehicleId);
                    }
                } else {
                    contactUsWithEmail(email);
                }
            }

            @Override
            public void negativeButton() {
                closedUnderReviewDialog();
                logOut();

            }
        };
        customDialogUnderReview.show();
    }

    public void closedUnderReviewDialog() {
        if (customDialogUnderReview != null && customDialogUnderReview.isShowing()) {
            customDialogUnderReview.dismiss();
            customDialogUnderReview = null;

        }
    }


    public void logOut() {
        Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_log_out), false, null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .logout(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Utils.hideCustomProgressDialog();
                            Utils.showMessageToast(response.body().getMessage(),
                                    BaseAppCompatActivity
                                            .this);
                            preferenceHelper.logout();// clear session token
                            preferenceHelper.putCheckEnd(false);
                            goToMainActivity();

                        } else {
                            Utils.hideCustomProgressDialog();
                            Utils.showErrorToast(response.body()
                                    .getErrorCode(), BaseAppCompatActivity.this);
                        }
                    }


                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(BaseAppCompatActivity.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.MAIN_DRAWER_ACTIVITY, e);
        }
    }

    protected void contactUsWithEmail(String email) {
        Uri gmmIntentUri = Uri.parse("mailto:" + email +
                "?subject=" + getString(R.string.text_request_to_admin) +
                "&body=" + getString(R.string.text_hello_sir));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.gm");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Utils.showToast(getResources().getString(R.string
                    .msg_google_mail_app_not_installed), this);
        }
    }


    public abstract void goWithBackArrow();


    public void setConnectivityListener(ConnectivityReceiverListener
                                                listener) {
        connectivityReceiverListener = listener;
        if (networkHelper != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkHelper.setNetworkAvailableListener(connectivityReceiverListener);
        }
    }

    public void setAdminApprovedListener(AdminApprovedListener
                                                 listener) {
        adminApprovedListener = listener;
    }

    public void goWithAdminApproved() {
        preferenceHelper.putIsApproved(Const.ProviderStatus.IS_APPROVED);
        goToMainDrawerActivity();
    }

    public void goWithAdminDecline() {
        preferenceHelper.putIsProviderOnline(Const.ProviderStatus
                .PROVIDER_STATUS_OFFLINE);
        preferenceHelper.putIsApproved(Const.ProviderStatus.IS_DECLINED);
        goToMainDrawerActivity();
    }

    public String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.handleException(BaseAppCompatActivity.class.getName(), e);
        }
        return null;
    }

    public void getProfileData()
    {
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
                public void onResponse(Call<ProviderDetailResponse> call, Response<ProviderDetailResponse> response) {
                    if (response.body().isSuccess()) {
                        Provider provider = response.body().getProvider();

                        preferenceHelper.putFirstName(provider
                                .getFirstName());
                        preferenceHelper.putLastName(provider
                                .getLastName());
                        preferenceHelper.putEmail(provider.getEmail());
                        preferenceHelper.putContact(provider.getPhone());
                        preferenceHelper.putProfilePic(IMAGE_BASE_URL+provider.getPicture());
                        preferenceHelper.putIdPendingProfile(response.body().getPendingprofileupdate());
                        setTitleOnToolbarMap("Hi,"+" "+preferenceHelper.getFirstName()+" !",preferenceHelper.getProfilePic());
                    }
                }

                @Override
                public void onFailure(Call<ProviderDetailResponse> call, Throwable t) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Intent getIntentForPermission() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * Remove  comment when you live eber in palyStore
         */
//        if (!TextUtils.equals(Locale.getDefault().getLanguage(), Const.EN)) {
//            openLanguageDialog();
//        }
    }

    private void openLanguageDialog() {

        final CustomDialogBigLabel customDialogBigLabel = new CustomDialogBigLabel(this,
                getResources()
                        .getString(R.string.text_attention), getResources()
                .getString(R.string.meg_language_not_an_english), getResources()
                .getString(R.string.text_settings),
                getResources()
                        .getString(R.string.text_exit_caps)) {
            @Override
            public void positiveButton() {
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
                dismiss();
            }

            @Override
            public void negativeButton() {
                dismiss();
                finishAffinity();

            }
        };
        customDialogBigLabel.show();
    }

    public void adjustFontScale(Configuration configuration) {
        if (configuration.fontScale > Const.DEFAULT_FONT_SCALE) {
            configuration.fontScale = Const.DEFAULT_FONT_SCALE;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            getBaseContext().getResources().updateConfiguration(configuration, metrics);
        }
    }

    public void hideKeyBord() {
        InputMethodManager inm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void restartApp() {
        startActivity(new Intent(this, SplashScreenActivity.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.wrapper(newBase, PreferenceHelper.getInstance
                (newBase).getLanguageCode()));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(appReceiver);
        super.onDestroy();
    }

    /**
     * this method used to make decision after login or register for screen transaction with
     * specific preference
     */
    public void moveWithUserSpecificPreference() {
        if(preferenceHelper.getIsUserPay()==1)
        {
            goToAddcardActivity();
        }
        else if(preferenceHelper.getIsHaveReferral() && preferenceHelper.getIsApplyReferral() == Const.FALSE) {
            goToReferralActivity();
        } else if (preferenceHelper.getAllDocUpload() == Const.FALSE) {
            goToDocumentActivity(false);
        } else {
            goToMainDrawerActivity();
        }
    }

    public class AppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Const.NETWORK_ACTION:
                        if (connectivityReceiverListener != null) {
                            connectivityReceiverListener.onNetworkConnectionChanged(Utils
                                    .isInternetConnected(context));
                        }
                        break;
                    case Const.GPS_ACTION:
                        if (connectivityReceiverListener != null) {
                            connectivityReceiverListener.onGpsConnectionChanged(Utils.isGpsEnable
                                    (context));
                        }
                        break;
                    case Const.ACTION_APPROVED_PROVIDER:
                        if (adminApprovedListener != null) {
                            adminApprovedListener.onAdminApproved();
                        }
                        break;
                    case Const.ACTION_DECLINE_PROVIDER:
                        if (adminApprovedListener != null) {
                            adminApprovedListener.onAdminDeclined();
                        }
                        break;
                    case Const.ACTION_NEW_TRIP:
                        if (preferenceHelper.getIsMainScreenVisible()) {
                            return;
                        } else {
                            if (!isScreenOn(context)) {
                                preferenceHelper.putIsScreenLock(true);
                            } else {
                                preferenceHelper.putIsScreenLock(false);
                            }
                            Intent mapIntent = new Intent(context, MainDrawerActivity.class);
                            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent
                                    .FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(mapIntent);
                        }
                        break;

                    case Const.PROFILE_APPROVED_ACTION:
                        if (adminApprovedListener != null) {
                            adminApprovedListener.onprofileApprove();
                        }
                        break;

                    default:
                        // do with default
                        break;
                }
            }
        }


    }

    public String validPhoneNumberMessage(int minDigit, int maxDigit) {
        if (maxDigit == minDigit) {
            return getResources().getString(R.string.msg_please_enter_valid_mobile_number,
                    maxDigit);
        } else {
            return getResources().getString(R.string.msg_please_enter_valid_mobile_number_between
                    , minDigit, maxDigit);
        }
    }
}
