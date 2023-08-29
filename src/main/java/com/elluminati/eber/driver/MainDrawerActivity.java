package com.elluminati.eber.driver;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.elluminati.eber.driver.adapter.CircularProgressViewAdapter;
import com.elluminati.eber.driver.adapter.DrawerAdapter;
import com.elluminati.eber.driver.components.CustomAddressChooseDialog;
import com.elluminati.eber.driver.components.CustomCircularProgressView;
import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.CustomDialogEnable;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.TopSheet.TopSheetBehavior;
import com.elluminati.eber.driver.fragments.FeedbackFragment;
import com.elluminati.eber.driver.fragments.InvoiceFragment;
import com.elluminati.eber.driver.fragments.MapFragment;
import com.elluminati.eber.driver.fragments.TripFragment;
import com.elluminati.eber.driver.interfaces.ClickListener;
import com.elluminati.eber.driver.interfaces.RecyclerTouchListener;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.singleton.AddressUtils;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.ImageHelper;
import com.elluminati.eber.driver.utils.KalmanLatLong;
import com.elluminati.eber.driver.utils.LocationHelper;
import com.elluminati.eber.driver.utils.SQLiteHelper;
import com.elluminati.eber.driver.utils.SpacesItemDecoration;
import com.elluminati.eber.driver.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainDrawerActivity extends BaseAppCompatActivity implements LocationHelper
        .OnLocationReceived {

    private static ScheduleListener scheduleListener;
    public Location currentLocation;
    public Location lastLocation;
    public LocationHelper locationHelper;
    public CurrentTrip currentTrip;
    public boolean isScheduleStart;
    public int countUpdateForLocation = 0;
    public ImageHelper imageHelper;
    public DrawerAdapter drawerAdapter;
    public MyFontButton btnGoOffline1;
    /**
     * this interval define provider location update time
     */
//    private DrawerLayout drawerLayout;
    private RecyclerView recycleView;
    private ScheduledExecutorService tripStatusSchedule;
    private LocationReceivedListener locationReceivedListener;
    private int drawerItemPosition;
    private CustomDialogBigLabel customDialogBigLabel;
    private CustomDialogEnable customDialogEnable;
    private Dialog dialogProgress;
    private CustomDialogBigLabel customCancelTripDialog;
    private SQLiteHelper sqLiteHelper;
    private KalmanLatLong kalmanLatLong;
    private IncomingHandler incomingHandler = new IncomingHandler();
    private LinearLayout llDrawerBg;
    private TopSheetBehavior topSheetBehavior;
    private NetworkListener networkListener;

    public void setNetworkListener(NetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    public void setLocationListener(LocationReceivedListener locationReceivedListener) {
        this.locationReceivedListener = locationReceivedListener;

    }

    public void setScheduleListener(ScheduleListener scheduleListener) {
        MainDrawerActivity.scheduleListener = scheduleListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_main_drawer);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setTurnScreenOn(true);
            this.setShowWhenLocked(true);
        }
        btnGoOffline1 = findViewById(R.id.btnGoOffline);
        btnGoOffline1.setOnClickListener(this);
        sqLiteHelper = new SQLiteHelper(this);
        currentTrip = CurrentTrip.getInstance();
        locationHelper = new LocationHelper(this);
        locationHelper.setLocationReceivedLister(this);
        imageHelper = new ImageHelper(this);
        initToolBar();
        initDrawer();

        locationHelper.checkLocationSetting(this);
        kalmanLatLong = new KalmanLatLong(25);

        checkLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        // Do noting for any click event
        drawerOpen();
    }


    @Override
    public void onClick(View v) {

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

    private void initDrawer() {
        LinearLayout llDrawer = findViewById(R.id.llDrawer);
        llDrawerBg = findViewById(R.id.llDrawerBg);
        topSheetBehavior = TopSheetBehavior.from(llDrawer);
        topSheetBehavior.setTopSheetCallback(new TopSheetBehavior.TopSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                llDrawerBg.setClickable(topSheetBehavior.getState() == TopSheetBehavior
                        .STATE_EXPANDED);

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                llDrawerBg.setAlpha(slideOffset);
                llDrawerBg.setVisibility(slideOffset == 0 ? View.GONE : View.VISIBLE);


            }
        });
        findViewById(R.id.ivClosedDrawerMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();

            }
        });
        findViewById(R.id.btnLogOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();
                openLogoutDialog();
            }
        });
        llDrawerBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();
            }
        });
        drawerAdapter = new DrawerAdapter(this);
        recycleView = (RecyclerView) findViewById(R.id.listViewDrawer);
        recycleView.setAdapter(drawerAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.addItemDecoration(new SpacesItemDecoration(getResources()
                .getDimensionPixelOffset(R.dimen.dimen_bill_line)));
        recycleView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recycleView, new ClickListener() {

            @Override
            public void onLongClick(View view, int position) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onClick(View view, int position) {
                // TODO Auto-generated method stub
                drawerItemPosition = position;
                switch (drawerItemPosition) {

                    case 0:
                        goToProfileActivity();
                        break;
                    case 1:
                        goToHistoryActivity();
                        break;
                    case 2:
                        goToDocumentActivity(true);
                        break;
                    case 3:
                        goToShowReferralActivity();
                        break;
                    case 4:
                        goToSettingsActivity();
                        break;
                    case 5:
                        gotoaddvihicle();
                        break;
                    case 6:
                        gotoDestinationActivity();
                        break;
                    case 7:
                        goToEarningActivity();
                        break;
                    case 8:
                        goToPaymentActivity();
                        break;
                    case 9:
                        goToContactUsActivity();
                        break;
                    case 10:
                        gotoBankDetailActivity();
                        break;

                    default:
                        // do with default
                        break;

                }


            }
        }));
    }


    private void loadFragmentsAccordingStatus() {
        if (TextUtils.isEmpty(preferenceHelper.getTripId())) {
            goToMapFragment();
        } else {
            goToTripFragment();
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        locationFilter(currentLocation);
        if (locationReceivedListener != null) {
            locationReceivedListener.onLocationReceived(location);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void goToHistoryActivity() {
        Intent historyIntent = new Intent(MainDrawerActivity.this,
                TripHistoryActivity.class);
        startActivity(historyIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(MainDrawerActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToContactUsActivity() {
        Intent intent = new Intent(MainDrawerActivity.this, ContactUsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationHelper.onStart();
        preferenceHelper.putIsMainScreenVisible(true);

        countUpdateForLocation = preferenceHelper.getCheckCountForLocation();
        if (preferenceHelper.getIsProviderOnline() == Const.ProviderStatus.PROVIDER_STATUS_ONLINE
                && preferenceHelper.getSessionToken() != null) {
            startLocationUpdateService();
        } else {
            stopLocationUpdateService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawerClosed();
        preferenceHelper.putIsMainScreenVisible(false);
        AppLog.Log(Const.Tag.MAIN_DRAWER_ACTIVITY, "onStop");
        stopTripStatusScheduled();
        preferenceHelper.putCheckCountForLocation(countUpdateForLocation);
        locationHelper.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * this method is start request scheduled which is used to update provide location and also
     * check is request is occurred
     */

    public void startTripStatusScheduled() {

        if (!isScheduleStart) {

            tripStatusSchedule = Executors.newSingleThreadScheduledExecutor();
            tripStatusSchedule.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    Message message = incomingHandler.obtainMessage();
                    incomingHandler.sendMessage(message);
                }
            }, 0, Const.SCHEDULED_SECONDS, TimeUnit
                    .SECONDS);
            AppLog.Log(Const.Tag.MAIN_DRAWER_ACTIVITY, "Schedule Start");
            isScheduleStart = true;
        }
    }

    public void stopTripStatusScheduled() {
        if (isScheduleStart) {
            AppLog.Log(Const.Tag.MAIN_DRAWER_ACTIVITY, "Schedule Stop");
            tripStatusSchedule.shutdown(); // Disable new tasks from being submitted
            // Wait a while for existing tasks to terminate
            try {
                if (!tripStatusSchedule.awaitTermination(60, TimeUnit.SECONDS)) {
                    tripStatusSchedule.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!tripStatusSchedule.awaitTermination(60, TimeUnit.SECONDS))
                        AppLog.Log(Const.Tag.MAIN_DRAWER_ACTIVITY, "Pool did not terminate");
                }
            } catch (InterruptedException e) {
                AppLog.handleException(Const.Tag.MAIN_DRAWER_ACTIVITY, e);
                // (Re-)Cancel if current thread also interrupted
                tripStatusSchedule.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();

            }
            isScheduleStart = false;
        }

    }


    @Override
    public void onBackPressed() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_EXPANDED) {
            drawerClosed();
        } else {
            openExitDialog(this);
        }

    }

    public void goToMapFragment() {
        if (getSupportFragmentManager().findFragmentByTag(Const.Tag.MAP_FRAGMENT) == null) {
            MapFragment mapFragment = new MapFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in_out,
                    R.anim.slide_out_left, R.anim.fade_in_out,
                    R.anim.slide_out_right);
            ft.replace(R.id.contain_frame, mapFragment, Const.Tag.MAP_FRAGMENT);
            ft.commitNowAllowingStateLoss();
        }
    }

    public void goToTripFragment() {
        if (getSupportFragmentManager().findFragmentByTag(Const.Tag.TRIP_FRAGMENT) == null) {
            TripFragment tripFragment = new TripFragment();
            addFragment(tripFragment, false, true, Const.Tag.TRIP_FRAGMENT);
        }
    }

    public void goToFeedBackFragment() {
        if (getSupportFragmentManager().findFragmentByTag(Const.Tag.FEEDBACK_FRAGMENT) == null) {
            FeedbackFragment feedbackFragment = new FeedbackFragment();
            addFragment(feedbackFragment, false, true, Const.Tag.FEEDBACK_FRAGMENT);
        }
    }

    public void goToInvoiceFragment() {
        if (getSupportFragmentManager().findFragmentByTag(Const.Tag.INVOICE_FRAGMENT) == null) {
            stopTripStatusScheduled();
            InvoiceFragment invoiceFragment = new InvoiceFragment();
            addFragment(invoiceFragment, false, true, Const.Tag.INVOICE_FRAGMENT);
        }
    }


    private void gotoBankDetailActivity() {
        Intent bankInfo = new Intent(this, BankDetailActivity.class);
        startActivity(bankInfo);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToSettingsActivity() {
        Intent settings = new Intent(this, SettingActivity.class);
        startActivity(settings);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToEarningActivity() {
        Intent earning = new Intent(this, EarningActivity.class);
        startActivity(earning);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void gotoaddvihicle() {
        Intent earning = new Intent(this, AddVehicle.class);
        startActivity(earning);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void gotoDestinationActivity() {
        Intent earning = new Intent(this, SetDestination.class);
        startActivity(earning);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /**
     * this method is start request  service which is used to update provide location and also
     * check is request is occurred
     */

    public void startLocationUpdateService() {
        if (!isMyServiceRunning(EberUpdateService.class)) {
            Intent intent = new Intent(this, EberUpdateService.class);
            intent.setAction(Const.Action.STARTFOREGROUND_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer
                .MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopLocationUpdateService() {
        if (isMyServiceRunning(EberUpdateService.class)) {
            Intent intent = new Intent(this, EberUpdateService.class);
            intent.setAction(Const.Action.STOPFOREGROUND_ACTION);
            stopService(intent);
        }
    }

    public void makePhoneCallToUser(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);

    }


    private void closedPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            customDialogEnable.dismiss();
            customDialogEnable = null;

        }
    }

    private void openPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources().getString(R.string
                .msg_reason_for_permission_location), getString(R.string.text_i_am_sure), getString
                (R.string.text_re_try)) {
            @Override
            public void doWithEnable() {
                ActivityCompat.requestPermissions(MainDrawerActivity.this, new String[]{Manifest
                        .permission
                        .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Const
                        .PERMISSION_FOR_LOCATION);
                closedPermissionDialog();
            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();
    }

    private void openPermissionNotifyDialog(final int code) {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources()
                .getString(R.string
                        .msg_permission_notification), getResources()
                .getString(R
                        .string.text_exit_caps), getResources().getString(R
                .string
                .text_settings)) {
            @Override
            public void doWithEnable() {
                closedPermissionDialog();
                startActivityForResult(getIntentForPermission(), code);

            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Const
                    .PERMISSION_FOR_LOCATION);
        } else {
            //Do the stuff that requires permission...
            loadFragmentsAccordingStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Const.PERMISSION_FOR_LOCATION:
                checkLocationPermission();
                break;
            default:
                // do with default
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            switch (requestCode) {
                case Const.PERMISSION_FOR_LOCATION:
                    goWithLocationPermission(grantResults);
                    break;
                default:
                    // do with default
                    break;
            }
        }
    }


    private void goWithLocationPermission(int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Do the stuff that requires permission...
            loadFragmentsAccordingStatus();
            stopLocationUpdateService();
            startLocationUpdateService();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                    .permission.ACCESS_COARSE_LOCATION) && ActivityCompat
                    .shouldShowRequestPermissionRationale(this, Manifest
                            .permission.ACCESS_FINE_LOCATION)) {
                openPermissionDialog();
            } else {
                openPermissionNotifyDialog(Const.PERMISSION_FOR_LOCATION);
            }
        }
    }


    public float getBearing(Location begin, Location end) {
        float bearing = 0;
        if (begin != null && end != null) {
            bearing = begin.bearingTo(end);
        }
        AppLog.Log("BEARING", String.valueOf(bearing));

        return bearing;
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (networkListener != null) {
            networkListener.onNetwork(isConnected);
        }
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
        getProfileData();

    }

    public void showProgressDialog(String message) {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            return;
        }

        CustomCircularProgressView ivProgressBar;
        MyFontTextView tvTitleProgress;
        dialogProgress = new Dialog(this);
        dialogProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogProgress.setContentView(R.layout.circuler_progerss_bar);
        tvTitleProgress = (MyFontTextView) dialogProgress.findViewById(R.id.tvTitleProgress);
        ivProgressBar = (CustomCircularProgressView) dialogProgress.findViewById(R.id
                .ivProgressBar);
        ivProgressBar.addListener(new CircularProgressViewAdapter() {
            @Override
            public void onProgressUpdate(float currentProgress) {
                Log.d("CPV", "onProgressUpdate: " + currentProgress);
            }

            @Override
            public void onProgressUpdateEnd(float currentProgress) {
                Log.d("CPV", "onProgressUpdateEnd: " + currentProgress);
            }

            @Override
            public void onAnimationReset() {
                Log.d("CPV", "onAnimationReset");
            }

            @Override
            public void onModeChanged(boolean isIndeterminate) {
                Log.d("CPV", "onModeChanged: " + (isIndeterminate ? "indeterminate" :
                        "determinate"));
            }
        });
        tvTitleProgress.setText(message);
        ivProgressBar.startAnimation();
        dialogProgress.setCancelable(false);
        WindowManager.LayoutParams params = dialogProgress.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogProgress.getWindow().setAttributes(params);
        dialogProgress.getWindow().setDimAmount(0);
        dialogProgress.show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable
                .ic_menu_black_24dp));
    }

    public void closedProgressDialog() {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }

    }


    public void openUserCancelTripDialog() {

        if (customCancelTripDialog != null && customCancelTripDialog.isShowing()) {
            return;
        }

        customCancelTripDialog = new CustomDialogBigLabel(this, getString(R.string
                .text_trip_cancelled), getString(R.string.message_trip_cancel), getString(R
                .string.text_ok), "") {
            @Override
            public void positiveButton() {
                closeUserCancelTripDialog();
            }

            @Override
            public void negativeButton() {

            }
        };
        customCancelTripDialog.show();
    }

    private void closeUserCancelTripDialog() {
        if (customCancelTripDialog != null && customCancelTripDialog.isShowing()) {
            customCancelTripDialog.dismiss();
            customCancelTripDialog = null;
        }
    }

    private void addAppInWhiteList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

    }

    private void goToPaymentActivity() {
        Intent paymentIntent = new Intent(MainDrawerActivity.this,
                PaymentActivity
                        .class);
        startActivity(paymentIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * this method filter fluctuate location
     *
     * @param location
     */
    public void locationFilter(Location location) {
        if (location == null)
            return;
        if (currentLocation == null) {
            currentLocation = location;
        }
        kalmanLatLong.Process(location.getLatitude(), location.getLongitude(),
                location.getAccuracy(), location.getTime());
        currentLocation.setLatitude(kalmanLatLong.get_lat());
        currentLocation.setLongitude(kalmanLatLong.get_lng());
        currentLocation.setAccuracy(kalmanLatLong.get_accuracy());
    }

    public void drawerClosed() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_EXPANDED) {
            topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
        }

    }

    public void drawerOpen() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_COLLAPSED) {
            topSheetBehavior.setState(TopSheetBehavior.STATE_EXPANDED);
        }

    }

    public void addFragment(Fragment fragment, boolean addToBackStack,
                            boolean isAnimate, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (isAnimate) {
            ft.setCustomAnimations(R.anim.slide_in_right,
                    R.anim.slide_out_left, R.anim.slide_in_left,
                    R.anim.slide_out_right);
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.contain_frame, fragment, tag);
        ft.commitNowAllowingStateLoss();
    }

    /**
     * Interface which help to get current location in fragment
     */

    public interface LocationReceivedListener {
        void onLocationReceived(Location location);

    }

    public interface ScheduleListener {
        void onSchedule();
    }

    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (scheduleListener != null) {
                scheduleListener.onSchedule();
            }

        }
    }

    public void setLastLocation(Location location) {
        if (location != null) {
            if (lastLocation == null) {
                lastLocation = new Location("lastLocation");
            }
            lastLocation.set(location);
        }


    }

    public interface NetworkListener {
        void onNetwork(boolean isConnected);
    }
}