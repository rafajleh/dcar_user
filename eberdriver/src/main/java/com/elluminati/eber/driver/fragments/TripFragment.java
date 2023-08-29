package com.elluminati.eber.driver.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.content.res.AppCompatResources;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.elluminati.eber.driver.ChatActivity;
import com.elluminati.eber.driver.MainDrawerActivity;
import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.components.CustomCircularProgressView;
import com.elluminati.eber.driver.components.CustomDialogNotification;
import com.elluminati.eber.driver.components.CustomDialogVerifyAccount;
import com.elluminati.eber.driver.components.CustomEventMapView;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyFontTextViewMedium;
import com.elluminati.eber.driver.mapUtils.PathDrawOnMap;
import com.elluminati.eber.driver.models.datamodels.CityType;
import com.elluminati.eber.driver.models.datamodels.Message;
import com.elluminati.eber.driver.models.datamodels.Trip;
import com.elluminati.eber.driver.models.datamodels.TripDetailOnSocket;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.responsemodels.TripPathResponse;
import com.elluminati.eber.driver.models.responsemodels.TripStatusResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.LatLngInterpolator;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.SQLiteHelper;
import com.elluminati.eber.driver.utils.SocketHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.emitter.Emitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elluminati.eber.driver.utils.Const.IMAGE_BASE_URL;

/**
 * Created by elluminati on 01-07-2016.
 */
public class TripFragment extends BaseFragments implements OnMapReadyCallback, MainDrawerActivity
        .LocationReceivedListener, PathDrawOnMap.GetResultFromPathDraw, MainDrawerActivity
        .ScheduleListener, ValueEventListener, SocketHelper.SocketListener,
        MainDrawerActivity.NetworkListener {
    private static int playLoopSound, playSoundBeforePickup;
    private GoogleMap googleMap;
    private CustomEventMapView tripMapView;
    private FloatingActionButton ivTipTargetLocation;
    private ImageView ivUserImage, btnCallCustomer;
    private MyFontButton btnJobStatus, btnReject, ivCancelTrip, btnAccept1;
    private LinearLayout llRequestAccept, llUpDateStatus, btnAccept;
    private TextView tvEstimateDistance, tvEstimateTime, tvMapPickupAddress,
            tvMapDestinationAddress, tvPaymentMode, tvTripNumber, tvEarn;
    private MyFontTextViewMedium tvUserName, tvEstLabel, tvDistanceLabel;
    private String destAddress, unit, cancelTripReason = "";
    private LatLng pickUpLatLng, destLatLng, lastLocationLatLng;
    private LinearLayout llTripNumber, llTotalDistance,
            llEarn;
    private int setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED;
    private static CountDownTimer countDownTimer;
    private static Timer tripTimer;
    private boolean isCountDownTimerStart, isWaitTimeCountDownTimerStart, isTripTimeCounter;
    private TripStatusReceiver tripStatusReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private ArrayList<LatLng> markerList;
    private LatLng currentLatLng;
    private SoundPool soundPool;
    private int tripRequestSoundId, pickupAlertSoundId;
    private PathDrawOnMap pathDrawOnMap;
    private boolean loaded, plays, playAlert, isTimerBackground;
    private boolean isGetPathFromServer = true;
    private Marker providerMarker, pickUpMarker, destinationMarker;
    private Polyline googlePathPolyline;
    private PolylineOptions currentPathPolylineOptions;
    private CustomDialogVerifyAccount tollDialog;
    private double tollPrice;
    private String destinationAddressCompleteTrip;
    private MyFontTextView tvScheduleTripTime, tvSpeed;
    private LinearLayout llScheduleTrip;
    private ImageView ivTripDriverCar, ivPickupDestination;
    private boolean doubleTabToEndTrip = false;
    private boolean isCameraIdeal = true;
    private SQLiteHelper sqLiteHelper;
    private int timerOneTimeStart = 0;
    private TripStatusResponse tripStatusResponse;
    private Trip trip;
    private Dialog tripProgressDialog;
    private boolean shouldUnbind;
    private ImageView ivHaveMessage;
    private NumberFormat currencyFormat;
    private ImageView btnChat;
    private TextView tvRentalTrip, tvRatting;
    private DatabaseReference databaseReference;
    private SocketHelper socketHelper;
    private ImageView ivYorFavouriteForUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeSoundPool();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable
                                     Bundle savedInstanceState) {
        View mapFragView = inflater.inflate(R.layout.fragment_trip, container, false);
        AppLog.Log("TripFarg", "onCreateView");
        tripMapView = (CustomEventMapView) mapFragView.findViewById(R.id.mapView);
        btnJobStatus = (MyFontButton) mapFragView.findViewById(R.id.btnJobStatus);
        llRequestAccept = (LinearLayout) mapFragView.findViewById(R.id.llRequestAccept);
        llUpDateStatus = (LinearLayout) mapFragView.findViewById(R.id.llUpDateStatus);
        btnReject = (MyFontButton) mapFragView.findViewById(R.id.btnReject);
        btnAccept = (LinearLayout) mapFragView.findViewById(R.id.llClientDetail);
        btnAccept1 = mapFragView.findViewById(R.id.btnAccept);

        tvEstimateDistance = mapFragView.findViewById(R.id.tvEstimateDistance);
        tvEstimateTime = mapFragView.findViewById(R.id.tvEstimateTime);
        btnCallCustomer = mapFragView.findViewById(R.id.btnCallCustomer);
        ivUserImage = (ImageView) mapFragView.findViewById(R.id.ivUserImage);
        tvUserName = (MyFontTextViewMedium) mapFragView.findViewById(R.id.tvUserName);
        tvMapPickupAddress = mapFragView.findViewById(R.id.tvMapPickupAddress);
        tvMapDestinationAddress = mapFragView.findViewById(R.id
                .tvMapDestinationAddress);
        tvPaymentMode = mapFragView.findViewById(R.id.tvPaymentMode);
        tvEstLabel = (MyFontTextViewMedium) mapFragView.findViewById(R.id.tvEstLabel);
        tvDistanceLabel = (MyFontTextViewMedium) mapFragView.findViewById(R.id.tvDistanceLabel);
        ivCancelTrip = (MyFontButton) mapFragView.findViewById(R.id.ivCancelTrip);
        ivTipTargetLocation = mapFragView.findViewById(R.id.ivTipTargetLocation);
        tvTripNumber = mapFragView.findViewById(R.id.tvTripNumber);
        llTripNumber = (LinearLayout) mapFragView.findViewById(R.id.llTripNumber);
        llTotalDistance = (LinearLayout) mapFragView.findViewById(R.id.llTotalDistance);
        llScheduleTrip = (LinearLayout) mapFragView.findViewById(R.id.llScheduleTrip);
        tvScheduleTripTime = (MyFontTextView) mapFragView.findViewById(R.id.tvScheduleTripTime);
        ivTripDriverCar = (ImageView) mapFragView.findViewById(R.id.ivTripDriverCar);
        llEarn = mapFragView.findViewById(R.id.llEarn);
        tvEarn = mapFragView.findViewById(R.id.tvEarn);
        tvSpeed = mapFragView.findViewById(R.id.tvSpeed);
        btnChat = mapFragView.findViewById(R.id.btnChat);
        tvRentalTrip = mapFragView.findViewById(R.id.tvRentalTrip);
        ivHaveMessage = mapFragView.findViewById(R.id.ivHaveMessage);
        tvRatting = mapFragView.findViewById(R.id.tvRatting);
        ivPickupDestination = mapFragView.findViewById(R.id.ivPickupDestination);
        ivYorFavouriteForUser = mapFragView.findViewById(R.id.ivYorFavouriteForUser);
        drawerActivity.btnGoOffline1.setVisibility(View.GONE);
        return mapFragView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restartLocationServiceIfReburied();
        currencyFormat =
                drawerActivity.currencyHelper.getCurrencyFormat(drawerActivity.preferenceHelper.getCurrencyCode());
        drawerActivity.setToolbarBackgroundAndElevation(false, R.color.color_white, 0);
        openTripProgressDialog();
        AppLog.Log("TripFarg", "onActivityCreated");
        sqLiteHelper = new SQLiteHelper(drawerActivity);
        tripMapView.onCreate(savedInstanceState);
        tripMapView.getMapAsync(this);
        pathDrawOnMap = new PathDrawOnMap(drawerActivity, this);
        markerList = new ArrayList<>();
        btnJobStatus.setOnClickListener(this);
        btnAccept.setOnClickListener(this);
        btnAccept1.setOnClickListener(this);

        btnReject.setOnClickListener(this);
        ivCancelTrip.setOnClickListener(this);
        btnCallCustomer.setOnClickListener(this);
        ivTipTargetLocation.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        tvRentalTrip.setOnClickListener(this);
        tripStatusReceiver = new TripStatusReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(drawerActivity);
        ivUserImage.setVisibility(View.VISIBLE);
        llTripNumber.setVisibility(View.VISIBLE);
        GlideApp.with(drawerActivity).load(drawerActivity.currentTrip.getUserProfileImage())
                .fallback(R.drawable.ellipse).placeholder(R.drawable.ellipse)
                .override(200, 200).dontAnimate()
                .into(ivUserImage);
        drawerActivity.locationHelper.getLastLocation(drawerActivity, new
                OnSuccessListener<Location>() {


                    @Override
                    public void onSuccess(Location location) {
                        drawerActivity.currentLocation = location;
                        setCurrentLatLag(location);
                    }
                });
        tvUserName.setText(drawerActivity.currentTrip.getUserFirstName() + " " +
                drawerActivity.currentTrip.getUserLastName());
        // currentLatLng = new LatLng(drawerActivity.locationHelper.getLastLocation().getLatitude
        // (), drawerActivity.locationHelper.getLastLocation().getLongitude());
        // doBindEberUpdateService();
        initFirebaseChat();
        socketHelper = SocketHelper.getInstance();
        socketHelper.setSocketConnectionListener(this);
        initCurrentPathDraw();

    }

    @Override
    public void onResume() {
        super.onResume();
        tripMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        drawerActivity.setScheduleListener(this);
        getTripStatus();
        drawerActivity.preferenceHelper.putIsHaveTrip(true);
        isTimerBackground = false;
        initTripStatusReceiver();
        if (isCountDownTimerStart) {
            playLoopSound();

        }
        if (databaseReference != null) {
            databaseReference.addValueEventListener(this);
        }
        receiveTripStatusUsingSocket(true);

    }

    @Override
    public void onPause() {
        tripMapView.onPause();
        AppLog.Log("TripFarg", "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        AppLog.Log("TripFarg", "onStop");
        stopLoopSound();
        stopWaitTimeCountDownTimer();
        isTimerBackground = true;
        localBroadcastManager.unregisterReceiver(tripStatusReceiver);
        if (databaseReference != null) {
            databaseReference.removeEventListener(this);
        }
        receiveTripStatusUsingSocket(false);
        stopTripTimeCounter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        AppLog.Log("TripFarg", "onSaveInstanceState");
        tripMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        tripMapView.onDestroy();
        socketHelper.setSocketConnectionListener(null);
        super.onDestroy();
    }


    private void setTripData() {
        CurrentTrip.getInstance().setTotalTime((int) trip.getTotalTime());
        tvRentalTrip.setVisibility(isCarRentalType() ? View.VISIBLE :
                View.GONE);
        String userName = trip.getUserFirstName() + " " + trip.getUserLastName();
        tvUserName.setText(userName);
        if (trip.isFixedFare() && trip.getProviderServiceFees() > 0) {
            llEarn.setVisibility(View.VISIBLE);
            tvEarn.setText(currencyFormat.format
                    (trip.getProviderServiceFees()));
        } else {
            llEarn.setVisibility(View.GONE);
        }
        GlideApp.with(drawerActivity.getApplicationContext())
                .load(IMAGE_BASE_URL + tripStatusResponse.getMapPinImageUrl())
                .override(drawerActivity.getResources().getDimensionPixelSize(R
                                .dimen.vehicle_pin_width)
                        , drawerActivity.getResources().getDimensionPixelSize(R
                                .dimen.vehicle_pin_height))
                .placeholder(R.drawable.driver_car)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivTripDriverCar);
        unit = Utils.showUnit(drawerActivity, trip.getUnit());
        tvTripNumber.setText(String.valueOf(trip.getUniqueId()));
        destAddress = trip.getDestinationAddress();
        tvMapPickupAddress.setText(trip.getSourceAddress());
        pickUpLatLng = new LatLng(trip.getSourceLocation().get(0), trip.getSourceLocation().get(1));
        if (trip.getDestinationLocation() != null && !trip.getDestinationLocation().isEmpty() &&
                trip
                        .getDestinationLocation()
                        .get(0)
                        !=
                        null &&
                trip
                        .getDestinationLocation().get
                        (1) !=
                        null) {
            destLatLng = new LatLng(trip.getDestinationLocation().get(0), trip
                    .getDestinationLocation().get(1));
        }
        setMarkerOnLocation(currentLatLng,
                pickUpLatLng, destLatLng, drawerActivity.currentLocation != null ?
                        drawerActivity.currentLocation.getBearing() : 0);
        drawerActivity.setLastLocation(drawerActivity.currentLocation);
        if (Const.CARD == trip.getPaymentMode()) {
            tvPaymentMode.setText(drawerActivity.getResources().getString(R
                    .string.text_card));
        } else {
            tvPaymentMode.setText(drawerActivity.getResources().getString(R
                    .string.text_cash));
        }
        tvRatting.setText(drawerActivity.parseContent.oneDigitDecimalFormat.format(CurrentTrip.getInstance().getUserRate()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llClientDetail:

                if (setProviderStatus == Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED) {
                    tripResponds(Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED, false);
                    btnAccept.setBackgroundTintList(drawerActivity.getResources().getColorStateList(R.color.color_app_trans_white2));
                }
                if (setProviderStatus == Const.ProviderStatus.PROVIDER_STATUS_TRIP_END) {
                   /* if (sqLiteHelper.getAllDBLocations().length() > 1) {
                        Utils.showLocationUpdateDialog(drawerActivity);
                    } else {
                        clickTwiceToEndTrip();
                    }*/
                    clickTwiceToEndTrip();
                } else {
                    if (setProviderStatus == Const.ProviderStatus
                            .PROVIDER_STATUS_TRIP_STARTED) {
                        providerLocationUpdateAtTripStartPoint();
                    }
                    updateProviderStatus(setProviderStatus);
                    isGetPathFromServer = true;
                }

                break;


            case R.id.btnAccept:
                if (setProviderStatus == Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED) {
                    tripResponds(Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED, false);
                    btnAccept.setBackgroundTintList(drawerActivity.getResources().getColorStateList(R.color.color_app_trans_white2));
                }
                break;

            case R.id.btnReject:
                tripResponds(Const.ProviderStatus.PROVIDER_STATUS_REJECTED, false);
                break;
            case R.id.btnJobStatus:

                if (setProviderStatus == Const.ProviderStatus.PROVIDER_STATUS_TRIP_END) {
                   /* if (sqLiteHelper.getAllDBLocations().length() > 1) {
                        Utils.showLocationUpdateDialog(drawerActivity);
                    } else {
                        clickTwiceToEndTrip();
                    }*/
                    clickTwiceToEndTrip();
                } else {
                    if (setProviderStatus == Const.ProviderStatus
                            .PROVIDER_STATUS_TRIP_STARTED) {
                        providerLocationUpdateAtTripStartPoint();
                    }
                    updateProviderStatus(setProviderStatus);
                    isGetPathFromServer = true;
                }

                break;
            case R.id.btnCallCustomer:
                if (!TextUtils.isEmpty(drawerActivity.currentTrip.getPhoneCountryCode
                        ()) && !TextUtils.isEmpty(drawerActivity.currentTrip.getUserPhone
                        ())) {
                    if (drawerActivity.preferenceHelper.getTwilioCallMaskEnable()) {
                        callUserViaTwilio();
                    } else {
                        drawerActivity.makePhoneCallToUser(drawerActivity.currentTrip.getPhoneCountryCode() + drawerActivity.currentTrip.getUserPhone());
                    }
                } else {
                    Utils.showToast(drawerActivity.getResources().getString(R.string
                                    .text_phone_not_available),
                            drawerActivity);
                }
                break;
            case R.id.ivToolbarIcon:
                if (setProviderStatus == Const.ProviderStatus
                        .PROVIDER_STATUS_TRIP_END || setProviderStatus == Const.ProviderStatus
                        .PROVIDER_STATUS_TRIP_STARTED) {
                    if (destAddress.isEmpty()) {
                        Utils.showToast(drawerActivity.getResources().getString(R.string
                                .text_no_destination), drawerActivity);
                    } else {
                        goToGoogleMapApp(destLatLng);
                    }

                } else {
                    goToGoogleMapApp(pickUpLatLng);
                }

                break;
            case R.id.ivCancelTrip:
                openCancelTripDialogReason();
                break;
            case R.id.ivTipTargetLocation:
                if (googleMap != null) {
                    drawerActivity.locationHelper.checkLocationSetting(drawerActivity);
                    setLocationBounds(true, markerList);
                }
                break;
            case R.id.btnChat:
                goToChatActivity();
                break;
            case R.id.tvRentalTrip:
                openRentalPackageDialog();
                break;
            default:
                // do with default
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        AppLog.Log(Const.Tag.TRIP_FRAGMENT, "GoogleMapReady");
        setUpMap();
        drawerActivity.setLocationListener(TripFragment.this);

    }

    private void setUpMap() {
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setPadding(0, drawerActivity.getResources().getDimensionPixelOffset(R
                        .dimen.dimen_horizontal_margin), 0,
                drawerActivity.getResources().getDimensionPixelOffset(R
                        .dimen.map_padding_bottom));

        this.googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        drawerActivity, R.raw.styleable_map));

    }

    @Override
    public void onLocationReceived(Location location) {
        setCurrentLatLag(location);
        if (trip != null) {
            setMarkerOnLocation(currentLatLng, pickUpLatLng,
                    destLatLng, drawerActivity.getBearing(drawerActivity.lastLocation,
                            location));
            drawerActivity.setLastLocation(location);
            if (trip.getIsProviderStatus() == Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED) {
                setTotalDistance(drawerActivity.currentTrip.getTotalDistance());

            }


        }
        float speed = location.getSpeed() * Const.KM_COEFFICIENT;
        if (!Float.isNaN(speed)) {
            tvSpeed.setText(drawerActivity.parseContent.singleDigit.format(speed));
        }

    }

    /**
     * this method is used to set marker on map according to trip status
     *
     * @param currentLatLng
     * @param pickUpLatLng
     * @param destLatLng
     */
    private void setMarkerOnLocation(LatLng currentLatLng, LatLng pickUpLatLng, LatLng
            destLatLng, float bearing) {
        if (googleMap != null) {
            BitmapDescriptor bitmapDescriptor;
            markerList.clear();
            boolean isBounce = false;

            if (currentLatLng != null) {
                if (providerMarker == null) {
                    bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Utils.drawableToBitmap
                            (AppCompatResources.getDrawable(drawerActivity, R.drawable
                                    .driver_car)));
                    providerMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng)
                            .title(drawerActivity.getResources().getString(R.string
                                    .text_my_location)
                            ).icon(bitmapDescriptor));
                    providerMarker.setAnchor(0.5f, 0.5f);
                    isBounce = true;

                } else {
                    if (ivTripDriverCar.getDrawable() != null) {
                        providerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Utils
                                .drawableToBitmap
                                        (ivTripDriverCar.getDrawable())));
                    }
                    animateMarkerToGB(providerMarker, currentLatLng, new LatLngInterpolator
                                    .Linear(),
                            bearing);
                }
                markerList.add(currentLatLng);

                if (pickUpLatLng != null) {
                    if (pickUpMarker == null) {
                        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Utils
                                .drawableToBitmap
                                        (AppCompatResources.getDrawable(drawerActivity, R
                                                .drawable.user_pin)));

                        pickUpMarker = googleMap.addMarker(new MarkerOptions().position
                                (pickUpLatLng)
                                .title(drawerActivity.getResources().getString(R.string
                                        .text_pick_up_loc))
                                .icon(bitmapDescriptor));
                    } else {
                        pickUpMarker.setPosition(pickUpLatLng);
                    }
                }

                int providerStatus = trip.getIsProviderStatus();
                if (providerStatus == Const.ProviderStatus
                        .PROVIDER_STATUS_ARRIVED || providerStatus == Const.ProviderStatus
                        .PROVIDER_STATUS_TRIP_STARTED || providerStatus == Const.ProviderStatus
                        .PROVIDER_STATUS_IDEAL ||
                        providerStatus == Const.ProviderStatus
                                .PROVIDER_STATUS_ACCEPTED_PENDING ||
                        providerStatus == Const.ProviderStatus
                                .PROVIDER_STATUS_REJECTED) {
                    if (destLatLng != null) {
                        markerList.add(destLatLng);
                    }
                } else {
                    if (pickUpLatLng != null) {
                        markerList.add(pickUpLatLng);
                    }
                }
                if (isBounce) {
                    try {
                        setLocationBounds(false, markerList);
                    } catch (Exception e) {
                        AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);

                    }
                }
            }

        }

    }

    private void setLocationBounds(boolean isCameraAnim, ArrayList<LatLng> markerList) {
        if (markerList != null && !markerList.isEmpty()) {
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            int driverListSize = markerList.size();
            for (int i = 0; i < driverListSize; i++) {
                bounds.include(markerList.get(i));
            }
            //Change the padding as per needed
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds.build(),
                    drawerActivity.getResources().getDimensionPixelOffset(R.dimen.map_padding));
            if (isCameraAnim) {
                googleMap.animateCamera(cu);
            } else {
                googleMap.moveCamera(cu);
            }
        }


    }

    /**
     * this method used to upDate UI  after request occur
     */
    private void updateUiAfterRequest() {
        llRequestAccept.setVisibility(View.VISIBLE);
        btnReject.setVisibility(View.VISIBLE);
        ivCancelTrip.setVisibility(View.GONE);
        updateUIWithAddresses(Const.SHOW_PICK_UP_ADDRESS);
        drawerActivity.tvTimeRemain.setVisibility(View.VISIBLE);
        llUpDateStatus.setVisibility(View.GONE);

    }

    /**
     * this method used when trip accepted by provider
     */
    private void updateUiWhenRequestAccept() {
        llUpDateStatus.setVisibility(View.VISIBLE);
        drawerActivity.setTitleOnToolbar(drawerActivity.getResources().getString(R.string
                .text_pick_up_address));
        drawerActivity.setToolbarIcon(AppCompatResources.getDrawable(drawerActivity, R.drawable
                .send), this);
        llRequestAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        ivCancelTrip.setVisibility(View.VISIBLE);
        drawerActivity.tvTimeRemain.setVisibility(View.GONE);
        // updateUIWithAddresses(Const.SHOW_PICK_UP_ADDRESS);
        updateUIWithAddresses(Const.SHOW_BOTH_ADDRESS);
    }

    /**
     * this method is used to update  address according to trip status
     *
     * @param addressUpdate
     */
    private void updateUIWithAddresses(int addressUpdate) {
        switch (addressUpdate) {
            case Const.SHOW_BOTH_ADDRESS:
                ivPickupDestination.setVisibility(View.VISIBLE);
                drawerActivity.setTitleOnToolbarMap("Hi,"+" "+drawerActivity.preferenceHelper.getFirstName()+" !",drawerActivity.preferenceHelper.getProfilePic());
                tvMapPickupAddress.setVisibility(View.VISIBLE);
                tvMapDestinationAddress.setVisibility(View.VISIBLE);
                tvMapPickupAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null
                        , null);
                tvMapDestinationAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        null, null
                        , null);
                if (destAddress.isEmpty()) {
                    tvMapDestinationAddress.setVisibility(View.GONE);
                } else {
                    tvMapDestinationAddress.setText(destAddress);
                }

                BitmapDescriptor bitmapDescriptor;
                if (destLatLng != null) {
                    if (destinationMarker == null) {
                        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Utils
                                .drawableToBitmap
                                        (AppCompatResources.getDrawable(drawerActivity, R
                                                .drawable.destination_pin)));
                        destinationMarker = googleMap.addMarker(new MarkerOptions().position
                                (destLatLng)
                                .title(drawerActivity.getResources().getString(R.string
                                        .text_drop_location))
                                .icon(bitmapDescriptor));
                    } else {

                        destinationMarker.setPosition(destLatLng);
                    }
                }


                break;
            case Const.SHOW_PICK_UP_ADDRESS:
                ivPickupDestination.setVisibility(View.GONE);
                drawerActivity.setTitleOnToolbar(drawerActivity.getResources().getString(R.string.text_pick_up_address));
                drawerActivity.setToolbarIcon(AppCompatResources.getDrawable(drawerActivity, R
                        .drawable
                        .send), this);
                tvMapPickupAddress.setVisibility(View.VISIBLE);
                tvMapDestinationAddress.setVisibility(View.GONE);
                tvMapPickupAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(drawerActivity, R.drawable.ic_source), null, null
                        , null);
                tvMapDestinationAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);



                break;
            case Const.SHOW_DESTINATION_ADDRESS:
                ivPickupDestination.setVisibility(View.GONE);
                drawerActivity.setTitleOnToolbar(drawerActivity.getResources().getString(R.string.text_destination_address));
                drawerActivity.setToolbarIcon(AppCompatResources.getDrawable(drawerActivity, R
                        .drawable
                        .send), this);
                tvMapPickupAddress.setVisibility(View.GONE);
                tvMapDestinationAddress.setVisibility(View.VISIBLE);
                tvMapDestinationAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(drawerActivity, R.drawable.ic_destination), null, null, null);
                tvMapPickupAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                if (!TextUtils.equals(tvMapDestinationAddress.getText().toString(), destAddress)) {
                    if (trip.getIsProviderStatus() == Const.ProviderStatus
                            .PROVIDER_STATUS_TRIP_STARTED || trip.getIsProviderStatus() == Const
                            .ProviderStatus
                            .PROVIDER_STATUS_ARRIVED) {
                        pathDrawOnMap.getPathDrawOnMap(pickUpLatLng, destLatLng,
                                drawerActivity.preferenceHelper.getIsPathDraw
                                        ());
                    }
                    if (TextUtils.isEmpty(destAddress)) {
                        tvMapDestinationAddress.setText(drawerActivity.getResources()
                                .getString(R.string.text_no_destination));
                    } else {
                        tvMapDestinationAddress.setText(destAddress);

                    }
                }
                break;
            default:
                // do with default
                break;
        }
    }

    /**
     * this method use to call WebService to for respondsTrip
     *
     * @param tripStatus
     */
    private synchronized void tripResponds(int tripStatus, boolean whenTimeOut) {
        stopCountDownTimer();
        JSONObject jsonObject = new JSONObject();
        Utils.showCustomProgressDialog(drawerActivity, "", false, null);
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.IS_PROVIDER_ACCEPTED, tripStatus);
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            if (Const.ProviderStatus.PROVIDER_STATUS_REJECTED == tripStatus) {
                jsonObject.put(Const.Params.IS_REQUEST_TIMEOUT, whenTimeOut);
            }

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .respondsTrip(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        stopCountDownTimer();
                        if (response.body().isSuccess()) {
                            if (Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED == response
                                    .body().getIsProviderAccepted()) {
                                updateUiWhenRequestAccept();
                                getTripStatus();
                            } else {
                                if (drawerActivity.preferenceHelper.getIsScreenLock()) {
                                    drawerActivity.preferenceHelper.putIsScreenLock(false);
                                    drawerActivity.finish();
                                }
                                drawerActivity.goToMapFragment();
                            }

                        } else {
                            drawerActivity.goToMapFragment();
                        }
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    /**
     * this method call WebService to know current trip status
     */
    public void getTripStatus() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            Call<TripStatusResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getTripStatus(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<TripStatusResponse>() {
                @Override
                public void onResponse(Call<TripStatusResponse> call,
                                       Response<TripStatusResponse> response) {
                    if (isAdded()) {
                        if (ParseContent.getInstance().isSuccessful(response)) {
                            if (response.body().isSuccess()) {
                                tripStatusResponse = response.body();
                                trip = response.body().getTrip();
                                ivYorFavouriteForUser.setVisibility(trip.isFavouriteProvider() ?
                                        View.VISIBLE : View.GONE);
                                checkIsTripAccepted(trip);
                                PreferenceHelper.getInstance(drawerActivity).putTripId(trip.getId
                                        ());
                                if (googleMap != null) {
                                    if (Const.ProviderStatus.PROVIDER_STATUS_TRIP_CANCELLED == trip
                                            .getIsTripCancelled()) {
                                        drawerActivity.goToMapFragment();
                                    } else {
                                        setTripData();
                                        checkCurrentTripStatus();
                                        checkProviderStatus();
                                        if (trip.getIsTripEnd() == Const.TRUE && trip.isTip()) {
                                            drawerActivity.closedProgressDialog();
                                            drawerActivity.goToInvoiceFragment();
                                        }
                                    }
                                }


                            } else {
                                if (response.body().getErrorCode() == Const.CODE_USER_CANCEL_TRIP) {
                                    stopCountDownTimer();
                                    drawerActivity.openUserCancelTripDialog();
                                }
                                drawerActivity.goToMapFragment();
                            }

                        }
                        hideTripProgressDialog();
                    }
                }

                @Override
                public void onFailure(Call<TripStatusResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    /**
     * this method call WebService to set provider status
     */
    private void updateProviderStatus(int providerStatus) {
        if (providerStatus == Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED) {
            setTotalTime(0);
            setTotalDistance(0);
        }
        setAccurateLocationFilter();
        Utils.showCustomProgressDialog(drawerActivity, drawerActivity.getResources()
                .getString(R.string.msg_loading), false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.IS_PROVIDER_STATUS, providerStatus);

            if (drawerActivity.currentLocation != null) {
                jsonObject.put(Const.Params.LATITUDE, drawerActivity.currentLocation
                        .getLatitude());
                jsonObject.put(Const.Params.LONGITUDE, drawerActivity.currentLocation
                        .getLongitude());
            }
            Call<TripStatusResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .setProviderStatus(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<TripStatusResponse>() {
                @Override
                public void onResponse(Call<TripStatusResponse> call,
                                       Response<TripStatusResponse> response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {

                        if (response.body().isSuccess()) {
                            Utils.hideCustomProgressDialog();
                            trip = response.body().getTrip();
                            checkProviderStatus();
                            emitTripStatusUsingSocket();
                        } else {
                            Utils.hideCustomProgressDialog();
                        }

                    }


                }

                @Override
                public void onFailure(Call<TripStatusResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    /**
     * this method to used set provider status and also get provider status on web
     */

    private void checkProviderStatus() {
        switch (trip.getIsProviderStatus()) {
            case Const.ProviderStatus.PROVIDER_STATUS_IDEAL:
                this.setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED;
                break;
            case Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED:
                this.setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_STARTED;
                setAccurateLocationFilter();
                getDistanceMatrix(currentLatLng, pickUpLatLng);
                //updateUIWithAddresses(Const.SHOW_PICK_UP_ADDRESS);
                updateUIWithAddresses(Const.SHOW_BOTH_ADDRESS);
                break;
            case Const.ProviderStatus.PROVIDER_STATUS_STARTED:
                this.setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_ARRIVED;
                btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                        .text_arrived));

                btnJobStatus.setBackgroundTintList(drawerActivity.getResources().getColorStateList(R.color.color_black));
                //  btnJobStatus.setBackgroundColor(getResources().getColor(R.color.color_black));
                setAccurateLocationFilter();
                getDistanceMatrix(currentLatLng, pickUpLatLng);
                //updateUIWithAddresses(Const.SHOW_PICK_UP_ADDRESS);
                updateUIWithAddresses(Const.SHOW_BOTH_ADDRESS);
                getTripPath();
                break;
            case Const.ProviderStatus.PROVIDER_STATUS_ARRIVED:
                stopSoundBeforePickup();
                this.setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED;
                btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                        .text_trip_start));
                btnJobStatus.setBackgroundTintList(drawerActivity.getResources().getColorStateList(R.color.color_app_divider_blue));
                //updateUIWithAddresses(Const.SHOW_DESTINATION_ADDRESS);
                updateUIWithAddresses(Const.SHOW_BOTH_ADDRESS);
                getTripPath();
                if (tripStatusResponse.getPriceForWaitingTime() > 0) {
                    startWaitTimeCountDownTimer(tripStatusResponse.getTotalWaitTime());
                }

                break;
            case Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED:
                stopWaitTimeCountDownTimer();
                this.setProviderStatus = Const.ProviderStatus.PROVIDER_STATUS_TRIP_END;
                btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                        .text_end_trip));
                btnJobStatus.setBackgroundTintList(drawerActivity.getResources().getColorStateList(R.color.color_app_divider_red));
                updateUiCancelTrip();
                //updateUIWithAddresses(Const.SHOW_DESTINATION_ADDRESS);
                updateUIWithAddresses(Const.SHOW_BOTH_ADDRESS);
                setTotalDistanceAndTime();
                getTripPath();
                break;

            case Const.ProviderStatus.PROVIDER_STATUS_TRIP_END:
                updateUiCancelTrip();
                if (trip.isTip() && trip.getIsTripEnd() == Const.FALSE) {
                    drawerActivity.showProgressDialog(drawerActivity.getResources().getString(R
                            .string
                            .msg_waiting_for_tip));
                } else if (trip.getIsTripEnd() == Const.TRUE) {
                    drawerActivity.closedProgressDialog();
                    drawerActivity.goToInvoiceFragment();
                }
                break;
            default:
                //do with default
                break;

        }

    }

    private void checkCurrentTripStatus() {
        btnChat.setVisibility(View.VISIBLE);
        switch (trip.getIsProviderAccepted()) {
            case Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED:
                stopCountDownTimer();
                updateUiWhenRequestAccept();
                break;
            case Const.ProviderStatus.PROVIDER_STATUS_ACCEPTED_PENDING:
            case Const.ProviderStatus.PROVIDER_STATUS_REJECTED:
                btnChat.setVisibility(View.GONE);
                getDistanceMatrix(currentLatLng, pickUpLatLng);
                updateUiAfterRequest();

                if (!isTimerBackground) {
                    startCountDownTimer(drawerActivity.currentTrip.getTimeLeft());
                }
                if (trip.getTripType() == Const.TripType.SCHEDULE_TRIP) {
                    llScheduleTrip.setVisibility(View.VISIBLE);
                    try {
                        SimpleDateFormat dateTime = new SimpleDateFormat(Const.TIME_FORMAT_AM,
                                Locale.US);
                        dateTime.setTimeZone(TimeZone.getTimeZone
                                (trip.getTimezone()));
                        tvScheduleTripTime.setText(drawerActivity.getResources().getString(R.string
                                .text_schedule_pickup_time) + " " + dateTime.format(ParseContent
                                .getInstance().webFormat.parse(trip.getScheduleStartTime())));
                    } catch (ParseException e) {
                        llScheduleTrip.setVisibility(View.GONE);
                        AppLog.handleException(TripFragment.class.getSimpleName(), e);
                    }

                }
                break;
            default:
                // do with default
                break;

        }
    }

    private void checkPaymentMode(int paymentMode) {
        switch (paymentMode) {
            case Const.CARD:
                tvPaymentMode.setText(drawerActivity.getResources().getString(R
                        .string.text_card));
                break;
            case Const.CASH:
                tvPaymentMode.setText(drawerActivity.getResources().getString(R
                        .string.text_cash));
                break;
            default:
                // do with default
                break;
        }
    }

    /**
     * this method call google DISTANCE_MATRIX_API  to get duration and distance status
     *
     * @param srcLatLng
     * @param destLatLng
     */
    private void getDistanceMatrix(LatLng srcLatLng, final LatLng destLatLng) {
        if (srcLatLng != null && destLatLng != null) {
            String origins = String.valueOf(srcLatLng.latitude + "," + srcLatLng.longitude);
            String destination = destLatLng.latitude + "," + destLatLng.longitude;
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Const.google.ORIGINS, origins);
            hashMap.put(Const.google.DESTINATIONS, destination);
            hashMap.put(Const.google.KEY, drawerActivity.preferenceHelper.getGoogleServerKey());
            ApiInterface apiInterface = new ApiClient().changeApiBaseUrl(Const.GOOGLE_API_URL)
                    .create(ApiInterface.class);
            Call<ResponseBody> call = apiInterface.getGoogleDistanceMatrix(hashMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        try {
                            HashMap<String, String> hashMap = drawerActivity.parseContent
                                    .parsDistanceMatrix(
                                            (response.body().string()));
                            if (hashMap != null) {
                                if (setProviderStatus == Const.ProviderStatus
                                        .PROVIDER_STATUS_TRIP_END) {
                                    destinationAddressCompleteTrip = hashMap.get(Const.google
                                            .DESTINATION_ADDRESSES);
                                    if (trip.isToll() && trip.getIsTripEnd() == Const.FALSE) {
                                        openTollDialog(destLatLng);
                                    } else {
                                        tollPrice = 0;
                                        checkDestination(destLatLng);
                                    }

                                } else {

                                    double distance = Double.valueOf(hashMap.get(Const.google
                                            .DISTANCE));
                                    double time = Double.valueOf(hashMap.get(Const.google
                                            .DURATION));
                                    if (unit.equals(drawerActivity.getResources().getString(R
                                            .string.unit_code_0)
                                    )) {
                                        distance = distance * 0.000621371;//convert in mile
                                    } else {
                                        distance = distance * 0.001;//convert in km
                                    }
                                    time = time / 60;// convert in mins
                                    if (trip.getIsProviderStatus() != Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED) {
                                        setTotalTime(time);
                                        setTotalDistance(distance);
                                    }
                                    if (trip.getIsProviderStatus() ==
                                            Const.ProviderStatus
                                                    .PROVIDER_STATUS_STARTED && Double.valueOf
                                            (hashMap
                                                    .get(Const.google
                                                            .DISTANCE)) <= Const.PICKUP_THRESHOLD &&
                                            drawerActivity.preferenceHelper
                                                    .getIsPickUpSoundOn()) {
                                        playSoundBeforePickup();
                                    }
                                }
                            } else {
                                if (setProviderStatus == Const.ProviderStatus
                                        .PROVIDER_STATUS_TRIP_END) {
                                    destinationAddressCompleteTrip = "";

                                    if (trip.isToll() && trip
                                            .getIsTripEnd() == Const.FALSE) {
                                        openTollDialog(destLatLng);
                                    } else {
                                        tollPrice = 0;
                                        checkDestination(destLatLng);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            AppLog.handleException(TripFragment.class.getSimpleName(), e);
                        }


                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        }
    }

    /**
     * this method call WebService to completeTrip
     */
    private void completeTrip(double destLat, double destLng, String destAddress, double
            tollAmount) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper
                    .getTripId
                            ());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.LATITUDE, destLat);
            jsonObject.put(Const.Params.LONGITUDE, destLng);
            jsonObject.put(Const.Params.DESTINATION_ADDRESS, destAddress);
            jsonObject.put(Const.Params.TOLL_AMOUNT, tollAmount);
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .completeTrip(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            emitTripStatusUsingSocket();
                            if (trip.isTip()) {
                                drawerActivity.showProgressDialog(drawerActivity.getResources()
                                        .getString(R
                                                .string
                                                .msg_waiting_for_tip));
                            } else {
                                payPayment();
                            }
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), drawerActivity);
                            btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                                    .text_end_trip));
                        }

                    }


                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    /**
     * this method is set countDown timer for count a trip accepting time
     *
     * @param seconds
     */
    private void startCountDownTimer(int seconds) {
        AppLog.Log("CountDownTimer", "Start");
        AppLog.Log("CountDownTimer", "second s remain" + seconds);
        if (seconds > 0) {
            if (!isCountDownTimerStart && isAdded() && timerOneTimeStart == 0) {
                timerOneTimeStart++;
                isCountDownTimerStart = true;
                final long milliSecond = 1000;
                long millisUntilFinished = seconds * milliSecond;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                countDownTimer = new CountDownTimer(millisUntilFinished, milliSecond) {
                    public void onTick(long millisUntilFinished) {
                        final long seconds = millisUntilFinished / milliSecond;
                        drawerActivity.tvTimeRemain.setText(String.valueOf(seconds) + "s " + "" +
                                drawerActivity.getResources().getString(R.string.text_remaining));
                        if (drawerActivity.preferenceHelper.getIsSoundOn()) {
                            if (!isTimerBackground)
                                playLoopSound();
                        } else {
                            stopLoopSound();
                        }
                    }

                    public void onFinish() {
                        // stopCountDownTimer();
                        tripResponds(Const.ProviderStatus.PROVIDER_STATUS_REJECTED, true);
                        stopLoopSound();

                    }
                }.start();
            }
        } else {
            tripResponds(Const.ProviderStatus.PROVIDER_STATUS_REJECTED, true);
        }
    }

    private void stopCountDownTimer() {
        AppLog.Log("CountDownTimer", "Stop");
        if (isCountDownTimerStart && countDownTimer != null) {
            isCountDownTimerStart = false;
            countDownTimer.cancel();
            countDownTimer = null;
            drawerActivity.tvTimeRemain.setText("");
        }
        stopLoopSound();
    }

    /**
     * this method is used to init sound pool for play sound file
     */
    private void initializeSoundPool() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    loaded = status == 0;
                }
            });
            /*tripRequestSoundId = soundPool.load(drawerActivity, R.raw.beep, 1);
            pickupAlertSoundId = soundPool.load(drawerActivity, R.raw
                    .driver_notify_before_pickup, 1);*/

        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    loaded = status == 0;
                }
            });
            /*tripRequestSoundId = soundPool.load(drawerActivity, R.raw.beep, 1);
            pickupAlertSoundId = soundPool.load(drawerActivity, R.raw
            .driver_notify_before_pickup, 1);*/
        }
        tripRequestSoundId = soundPool.load(drawerActivity, R.raw.beep, 1);
        pickupAlertSoundId = soundPool.load(drawerActivity, R.raw
                .driver_notify_before_pickup, 1);
    }

    public void playLoopSound() {
        // Is the sound loaded does it already play?
        if (loaded && !plays) {
            // the sound will play for ever if we put the loop parameter -1
            playLoopSound = soundPool.play(tripRequestSoundId, 1, 1, 1, -1, 0.5f);
            plays = true;
        }
    }

    public void stopLoopSound() {
        if (plays) {
            soundPool.stop(playLoopSound);
            tripRequestSoundId = soundPool.load(drawerActivity, R.raw.beep, 1);
            plays = false;
        }
    }

    public void playSoundBeforePickup() {
        // Is the soniund loaded does it already play?
        if (loaded && !playAlert) {
            // the sound will play for ever if we put the loop parameter -1
            playSoundBeforePickup = soundPool.play(pickupAlertSoundId, 1, 1, 1, 0, 1f);
            playAlert = true;
        }
    }

    public void stopSoundBeforePickup() {
        if (playAlert) {
            soundPool.stop(playSoundBeforePickup);
            pickupAlertSoundId = soundPool.load(drawerActivity, R.raw
                    .driver_notify_before_pickup, 1);
            playAlert = false;
        }
    }

    @Override
    public void getPathResult(PolylineOptions polylineOptions, String routes, int serviceCode) {

        if (polylineOptions != null) {
           /* setMarkerOnLocation(currentLatLng, pickUpLatLng,
                    destLatLng, 0);*/
            if (googlePathPolyline != null) {
                googlePathPolyline.remove();
            }
            googlePathPolyline = this.googleMap.addPolyline(polylineOptions);
            if (serviceCode == Const.ServiceCode.PATH_DRAW) {
                switch (trip.getIsProviderStatus()) {
                    case Const.ProviderStatus.PROVIDER_STATUS_STARTED:
                        updateGooglePathStartLocationToPickUpLocation(routes);
                        break;
                    case Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED:
                        updateGooglePickUpLocationToDestinationLocation(routes);
                        break;
                    default:
                        // do with default
                        break;
                }
            }

        }

    }


    /**
     * this method is used to open Google Map app whit given LatLng
     *
     * @param destination
     */
    private void goToGoogleMapApp(LatLng destination) {
        if (destination != null) {
            String latitude = String.valueOf(destination.latitude);
            String longitude = String.valueOf(destination.longitude);
            String url = "waze://?ll=" + latitude + ", " + longitude + "&navigate=yes";
            Intent intentWaze = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intentWaze.setPackage("com.waze");

            String uriGoogle = "google.navigation:q=" + latitude + "," + longitude;
            Intent intentGoogleNav = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle));
            intentGoogleNav.setPackage("com.google.android.apps.maps");

            String title = drawerActivity.getString(R.string.app_name);
            Intent chooserIntent = Intent.createChooser(intentGoogleNav, title);
            Intent[] arr = new Intent[1];
            arr[0] = intentWaze;
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr);
            drawerActivity.startActivity(chooserIntent);
        }

    }

    private void openCancelTripDialogReason() {

        final Dialog cancelTripDialog = new Dialog(drawerActivity);
        RadioGroup dialogRadioGroup;
        final MyFontEdittextView etOtherReason;
        final RadioButton rbReasonOne, rbReasonTwo, rbReasonThree, rbReasonOther;
        cancelTripDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cancelTripDialog.setContentView(R.layout.dialog_cancle_trip_reason);
        etOtherReason = (MyFontEdittextView) cancelTripDialog.findViewById(R.id.etOtherReason);
        rbReasonOne = (RadioButton) cancelTripDialog.findViewById(R.id.rbReasonOne);
        rbReasonTwo = (RadioButton) cancelTripDialog.findViewById(R.id.rbReasonTwo);
        rbReasonThree = (RadioButton) cancelTripDialog.findViewById(R.id.rbReasonThree);
        rbReasonOther = (RadioButton) cancelTripDialog.findViewById(R.id.rbReasonOther);
        dialogRadioGroup = (RadioGroup) cancelTripDialog.findViewById(R.id.dialogRadioGroup);
        cancelTripDialog.findViewById(R.id.btnIamSure).setOnClickListener(new View
                .OnClickListener() {


            @Override
            public void onClick(View v) {
                if (rbReasonOther.isChecked()) {
                    cancelTripReason = etOtherReason.getText().toString();
                }

                if (!cancelTripReason.isEmpty()) {
                    cancelTrip(cancelTripReason);
                    cancelTripDialog.dismiss();
                } else {
                    Utils.showToast(drawerActivity.getResources().getString(R
                            .string.msg_plz_give_valid_reason), drawerActivity);
                }

            }
        });
        cancelTripDialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener
                () {


            @Override
            public void onClick(View v) {
                cancelTripReason = "";
                cancelTripDialog.dismiss();
            }
        });


        dialogRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbReasonOne:
                        cancelTripReason = rbReasonOne.getText().toString();
                        etOtherReason.setVisibility(View.GONE);
                        break;
                    case R.id.rbReasonTwo:
                        cancelTripReason = rbReasonTwo.getText().toString();
                        etOtherReason.setVisibility(View.GONE);
                        break;
                    case R.id.rbReasonThree:
                        cancelTripReason = rbReasonThree.getText().toString();
                        etOtherReason.setVisibility(View.GONE);
                        break;
                    case R.id.rbReasonOther:
                        etOtherReason.setVisibility(View.VISIBLE);
                        break;
                    default:
                        // do with default
                        break;
                }

            }
        });
        WindowManager.LayoutParams params = cancelTripDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        cancelTripDialog.getWindow().setAttributes(params);
        cancelTripDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancelTripDialog.setCancelable(false);
        cancelTripDialog.show();

    }

    /**
     * this method is used to cancel trip
     */
    public void cancelTrip(String
                                   cancelReason) {
        Utils.showCustomProgressDialog(drawerActivity, drawerActivity.getResources().getString(R
                .string
                .msg_waiting_for_cancel_trip), false, null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.CANCEL_REASON, cancelReason);
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .cancelTrip(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    Utils.hideCustomProgressDialog();
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            drawerActivity.currentTrip.clearData();
                            drawerActivity.goToMapFragment();
                            emitTripStatusUsingSocket();
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), drawerActivity);
                        }
                    }


                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                    Utils.hideCustomProgressDialog();
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.MAP_FRAGMENT, e);
        }
    }


    private void setTotalDistanceAndTime() {
        tvDistanceLabel.setText(drawerActivity.getResources().getString(R.string
                .text_total_distance));
        tvEstLabel.setText(drawerActivity.getResources().getString(R.string.text_total_time));
        // distance and time must be grater then 0
        setTotalDistance(drawerActivity.currentTrip.getTotalDistance());
        setTotalTime(drawerActivity.currentTrip.getTotalTime());
        startTripTimeCounter(drawerActivity.currentTrip.getTotalTime());

    }

    private void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final
    LatLngInterpolator latLngInterpolator, final float bearing) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(finalPosition.latitude, finalPosition
                    .longitude);

            final float startRotation = marker.getRotation();
            final LatLngInterpolator interpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition,
                                endPosition);
                        marker.setPosition(newPosition);
                        marker.setAnchor(0.5f, 0.5f);
                      /*  marker.setRotation(getBearing(startPosition, new LatLng(finalPosition
                                .latitude, finalPosition.longitude)));*/
                      /*  googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new
                                CameraPosition.Builder()
                                .target(newPosition)
                                .zoom(15.5f)
                                .build()));*/
                        if (isCameraIdeal && getDistanceBetweenTwoLatLng(startPosition,
                                finalPosition) > Const.DISPLACEMENT) {
                            updateCamera(getBearing(startPosition, new LatLng(finalPosition
                                            .latitude, finalPosition.longitude)),
                                    newPosition);
                        }


                    } catch (Exception ex) {
                        //I don't care atm..
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition)
                    // .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                }
            });
            valueAnimator.start();
        }

    }

    private void updateCamera(float bearing, LatLng positionLatLng) {
        if (googleMap != null) {
            isCameraIdeal = false;
            CameraPosition oldPos = googleMap.getCameraPosition();

            CameraPosition pos = CameraPosition.builder(oldPos).target
                    (positionLatLng).zoom(17f).bearing(bearing).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 3000,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            AppLog.Log("CAMERA", "FINISH");
                            isCameraIdeal = true;
                            drawCurrentPath();

                        }

                        @Override
                        public void onCancel() {
                            isCameraIdeal = true;
                            AppLog.Log("CAMERA", "cancelling camera");
                            drawCurrentPath();

                        }
                    });
        }
    }

    private void initTripStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_CANCEL_TRIP);
        intentFilter.addAction(Const.ACTION_PROVIDER_TRIP_END);
        intentFilter.addAction(Const.ACTION_PAYMENT_CARD);
        intentFilter.addAction(Const.ACTION_PAYMENT_CASH);
        intentFilter.addAction(Const.ACTION_DESTINATION_UPDATE);
        intentFilter.addAction(Const.ACTION_TRIP_ACCEPTED_BY_ANOTHER_PROVIDER);
        localBroadcastManager.registerReceiver(tripStatusReceiver, intentFilter);
    }

    /*private void clickTwiceToEndTrip() {
        if (isDoublePressed) {
            Utils.showCustomProgressDialog(drawerActivity, drawerActivity.getResources()
                    .getString(R.string.msg_waiting_for_trip_end), false, null);
            Location lastLocation = drawerActivity.locationHelper.getLastLocation();
            lastLocationLatLng = new LatLng(lastLocation.getLatitude(), lastLocation
                    .getLongitude());
            getDistanceMatrix(pickUpLatLng, lastLocationLatLng);
            return;
        } else {
            this.isDoublePressed = true;
            Toast.makeText(drawerActivity, drawerActivity.getResources().getString(R.string
                    .text_click_twice), Toast
                    .LENGTH_SHORT)
                    .show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    isDoublePressed = false;
                }
            }, 2000);
        }
    }*/

    private void updateGooglePathStartLocationToPickUpLocation(String routes) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.GOOGLE_PATH_START_LOCATION_TO_PICKUP_LOCATION,
                    routes);
            jsonObject.put(Const.Params.GOOGLE_PICKUP_LOCATION_TO_DESTINATION_LOCATION, "");
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .setTripPath(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    private void updateGooglePickUpLocationToDestinationLocation(String routes) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.GOOGLE_PATH_START_LOCATION_TO_PICKUP_LOCATION, "");
            jsonObject.put(Const.Params.GOOGLE_PICKUP_LOCATION_TO_DESTINATION_LOCATION,
                    routes);
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .setTripPath(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    private void getTripPath() {
        if (drawerActivity.preferenceHelper.getIsPathDraw() && isGetPathFromServer && trip !=
                null) {
            isGetPathFromServer = false;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper
                        .getTripId
                                ());
                jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                        .getSessionToken());
                jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                        .getProviderId());

                Call<TripPathResponse> call = ApiClient.getClient().create(ApiInterface
                        .class).getTripPath(ApiClient.makeJSONRequestBody(jsonObject));
                call.enqueue(new Callback<TripPathResponse>() {
                    @Override
                    public void onResponse(Call<TripPathResponse> call,
                                           Response<TripPathResponse> response) {
                        if (ParseContent.getInstance().isSuccessful(response)) {
                            if (response.body().isSuccess()) {

                                switch (trip.getIsProviderStatus()) {
                                    case Const.ProviderStatus.PROVIDER_STATUS_STARTED:
                                        String responseStartToPicUp = response.body()
                                                .getTriplocation()
                                                .getGooglePathStartLocationToPickUpLocation();
                                        if (TextUtils.isEmpty(responseStartToPicUp)) {
                                            LatLng currentLatLng = new LatLng(drawerActivity
                                                    .currentLocation
                                                    .getLatitude(), drawerActivity
                                                    .currentLocation.getLongitude());
                                            pathDrawOnMap.getPathDrawOnMap(currentLatLng,
                                                    pickUpLatLng,
                                                    drawerActivity.preferenceHelper.getIsPathDraw
                                                            ());
                                        } else {
                                            pathDrawOnMap.new ParserTask(Const.ServiceCode
                                                    .GET_GOOGLE_MAP_PATH)
                                                    .execute(responseStartToPicUp);
                                        }
                                        break;
                                    case Const.ProviderStatus.PROVIDER_STATUS_TRIP_STARTED:
                                    case Const.ProviderStatus.PROVIDER_STATUS_ARRIVED:
                                        String responsePicUpToDestination = response.body()
                                                .getTriplocation()
                                                .getGooglePickUpLocationToDestinationLocation();
                                        if (destLatLng != null) {
                                            if (TextUtils.isEmpty(responsePicUpToDestination)) {
                                                pathDrawOnMap.getPathDrawOnMap(pickUpLatLng,
                                                        destLatLng,
                                                        drawerActivity.preferenceHelper
                                                                .getIsPathDraw());
                                            } else {
                                                pathDrawOnMap.new ParserTask(Const.ServiceCode
                                                        .GET_GOOGLE_MAP_PATH).execute
                                                        (responsePicUpToDestination);
                                            }
                                        } else {
                                            AppLog.Log(Const.Tag.TRIP_FRAGMENT, "destLatLng = " +
                                                    destLatLng);
                                        }
                                        if (trip.getIsProviderStatus() ==
                                                Const.ProviderStatus
                                                        .PROVIDER_STATUS_TRIP_STARTED) {
                                            List<List<Double>> locationList = response.body()
                                                    .getTriplocation()
                                                    .getStartTripToEndTripLocations();
                                            if (!locationList.isEmpty()) {
                                                int size = locationList.size();
                                                for (int i = 0; i < size; i++) {
                                                    List<Double> locations = locationList.get(i);
                                                    LatLng latLng = new LatLng(locations.get(0),
                                                            locations.get(1));
                                                    currentPathPolylineOptions.add(latLng);
                                                }
                                            }


                                        }
                                        break;
                                    default:
                                        // do with default
                                        break;
                                }
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<TripPathResponse> call, Throwable t) {
                        AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                    }
                });

            } catch (JSONException e) {
                AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
                isGetPathFromServer = true;
            }
        }
    }

    private void drawCurrentPath() {
        if (trip.getIsProviderStatus() == Const.ProviderStatus
                .PROVIDER_STATUS_TRIP_STARTED) {
            currentPathPolylineOptions.add(currentLatLng);
            googleMap.addPolyline(currentPathPolylineOptions);
        }


    }

    private void initCurrentPathDraw() {
        currentPathPolylineOptions = new PolylineOptions();
        currentPathPolylineOptions.color(ResourcesCompat.getColor(drawerActivity.getResources(),
                R.color.color_app_red_path, null));
        currentPathPolylineOptions.width(15);
    }

    private void startWaitTimeCountDownTimer(final int seconds) {
        if (isAdded()) {
            AppLog.Log("SECONDS", seconds + "");
            AppLog.Log("WaitTimeCountDownTimerStart", "Start");
            if (!isWaitTimeCountDownTimerStart) {
                updateUiForWaitingTime(true);
                isWaitTimeCountDownTimerStart = true;
                final long milliSecond = 1000;
                final long totalSeconds = 86400 * milliSecond;
                countDownTimer = null;
                countDownTimer = new CountDownTimer(totalSeconds, milliSecond) {

                    long remain = seconds;

                    public void onTick(long millisUntilFinished) {
                        remain = remain + 1;
                        if (remain > 0 && TextUtils.equals(tvEstLabel.getText().toString(),
                                drawerActivity.getResources().getString(R.string.text_wait_time_start_after))) {

                            tvEstLabel.setText(drawerActivity.getResources().getString(R.string.text_wait_time));
                        }

                        if (remain < 0) {
                            tvEstimateTime.setText(String.valueOf(remain * (-1)) + "s");
                        } else {
                            tvEstimateTime.setText(String.valueOf(remain) + "s");
                        }
                    }

                    public void onFinish() {
                        isWaitTimeCountDownTimerStart = false;
                    }

                }.start();
            }
        }
    }

    private void stopWaitTimeCountDownTimer() {
        AppLog.Log("WaitTimeCountDownTimerStart", "Stop");
        if (isWaitTimeCountDownTimerStart) {
            updateUiForWaitingTime(false);
            isWaitTimeCountDownTimerStart = false;
            countDownTimer.cancel();
        }
    }

    private void updateUiForWaitingTime(boolean isUpdate) {
        if (isUpdate) {
            llTotalDistance.setVisibility(View.GONE);
            tvEstLabel.setText(drawerActivity.getResources().getString(R.string
                    .text_wait_time_start_after));
        } else {
            llTotalDistance.setVisibility(View.VISIBLE);
        }

    }

    /**
     * trip timer used to count a trip time when trip is started
     *
     * @param minute
     */

    private void startTripTimeCounter(final int minute) {
        if (isAdded()) {
            AppLog.Log("MINUTE", minute + "");
            AppLog.Log("TripTimeCounter", "Start");
            if (!isTripTimeCounter) {
                isTripTimeCounter = true;
                tripTimer = null;
                tripTimer = new Timer();
                tripTimer.scheduleAtFixedRate(new TimerTask() {
                    int count = minute;

                    @Override
                    public void run() {
                        drawerActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTotalTime(count);
                                count++;
                            }
                        });

                    }
                }, 1000, 60000);
            }
        }
    }

    /**
     * Use to stop trip timer.
     */
    private void stopTripTimeCounter() {
        if (isTripTimeCounter) {
            AppLog.Log("TripTimeCounter", "Stop");
            isTripTimeCounter = false;
            tripTimer.cancel();
        }
    }

    private void payPayment() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.TIP_AMOUNT, 0);
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .payPayment
                            (ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            emitTripStatusUsingSocket();
                            drawerActivity.closedProgressDialog();
                            drawerActivity.goToInvoiceFragment();
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), drawerActivity);
                        }
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.MAIN_DRAWER_ACTIVITY, e);
        }
    }

    private void openTollDialog(final LatLng destLatLng) {
        if (tollDialog != null && tollDialog.isShowing()) {
            return;
        }

        tollDialog = new CustomDialogVerifyAccount(getActivity(),
                drawerActivity.getResources().getString(R
                        .string.text_toll_dialog_title),
                drawerActivity.getResources().getString(R.string.text_apply),
                drawerActivity.getResources().getString(R.string
                        .text_cancel),
                drawerActivity.getResources().getString(R.string.text_enter_toll_amount), true) {
            @Override
            public void doWithEnable(EditText editText) {
                String tollAmount = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(tollAmount)) {
                    try {
                        tollPrice = Double.valueOf(tollAmount);
                        checkDestination(destLatLng);
                        /*completeTrip(lastLocationLatLng.latitude, lastLocationLatLng.longitude,
                                destinationAddress , Double.valueOf(tollAmount));*/
                        tollDialog.dismiss();
                    } catch (NumberFormatException e) {
                        Utils.showToast(drawerActivity.getResources().getString(R.string
                                .text_plz_enter_amount), drawerActivity);
                    }
                } else {
                    Utils.showToast(drawerActivity.getResources().getString(R.string
                            .text_plz_enter_amount), drawerActivity);
                }
            }

            @Override
            public void doWithDisable() {
                tollPrice = 0;
                checkDestination(destLatLng);
                /*completeTrip(lastLocationLatLng.latitude, lastLocationLatLng.longitude,
                        destinationAddress , 0);*/
                tollDialog.dismiss();
            }

            @Override
            public void clickOnText() {

            }
        };
        tollDialog.setInputTypeNumber();
        tollDialog.show();
    }

    private void checkDestination(LatLng destLatLng) {
        if (destLatLng != null) {
            if (trip != null && (trip.isFixedFare() || isCarRentalType())) {
                completeTrip(destLatLng.latitude, destLatLng.longitude,
                        destinationAddressCompleteTrip, tollPrice);
            } else {
                Utils.showCustomProgressDialog(drawerActivity, "Check Destination", false, null);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                            .getProviderId());
                    jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                            ());
                    jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                            .getSessionToken());
                    jsonObject.put(Const.Params.LATITUDE, destLatLng.latitude);
                    jsonObject.put(Const.Params.LONGITUDE, destLatLng.longitude);

                    Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                            .checkDestination(ApiClient.makeJSONRequestBody(jsonObject));
                    call.enqueue(new Callback<IsSuccessResponse>() {
                        @Override
                        public void onResponse(Call<IsSuccessResponse> call,
                                               Response<IsSuccessResponse>
                                                       response) {
                            if (ParseContent.getInstance().isSuccessful(response)) {
                                if (response.body().isSuccess()) {
                                    completeTrip(lastLocationLatLng.latitude, lastLocationLatLng
                                                    .longitude,
                                            destinationAddressCompleteTrip, tollPrice);
                                } else {
                                    Utils.showErrorToast(response.body().getErrorCode(),
                                            drawerActivity);
                                }
                            }


                        }

                        @Override
                        public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                            AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                        }
                    });
                } catch (JSONException e) {
                    AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
                }
            }
        } else {
            Utils.showToast(drawerActivity.getResources().getString(R.string
                    .text_location_not_found), drawerActivity);
        }
    }


    private void clickTwiceToEndTrip() {
        if (doubleTabToEndTrip) {
            doubleTabToEndTrip = false;
            Utils.showCustomProgressDialog(drawerActivity, drawerActivity.getResources()
                    .getString(R.string.msg_waiting_for_trip_end), false, null);
            btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                    .text_trip_is_ending));
            drawerActivity.locationHelper.getLastLocation(drawerActivity, new
                    OnSuccessListener<Location>() {


                        @Override
                        public void onSuccess(Location location) {
                            drawerActivity.currentLocation = location;
                            if (drawerActivity.currentLocation != null) {
                                drawerActivity.locationFilter(location);
                                lastLocationLatLng = new LatLng(drawerActivity.currentLocation
                                        .getLatitude(), drawerActivity.currentLocation
                                        .getLongitude());
                                getDistanceMatrix(pickUpLatLng, lastLocationLatLng);
                            } else {
                                Utils.hideCustomProgressDialog();
                                Utils.showToast(drawerActivity.getResources().getString(R.string
                                                .text_location_not_found),
                                        drawerActivity);
                            }


                        }
                    });
            return;
        }
        doubleTabToEndTrip = true;
        btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                .text_tab_again_end_trip));
        Utils.showToast(drawerActivity.getResources().getString(R.string
                .text_tab_again_end_trip), drawerActivity);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (doubleTabToEndTrip) {
                    btnJobStatus.setText(drawerActivity.getResources().getString(R.string
                            .text_end_trip));
                }
                doubleTabToEndTrip = false;
            }
        }, 300);
    }

    private void updateUiCancelTrip() {
        ivCancelTrip.setVisibility(View.GONE);
    }

    private void setCurrentLatLag(Location location) {
        if (location != null) {
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private float getDistanceBetweenTwoLatLng(LatLng startLatLng, LatLng endLatLang) {
        Location startLocation = new Location("start");
        Location endlocation = new Location("end");
        endlocation.setLatitude(endLatLang.latitude);
        endlocation.setLongitude(endLatLang.longitude);
        startLocation.setLatitude(startLatLng.latitude);
        startLocation.setLongitude(startLatLng.longitude);
        float distance = startLocation.distanceTo(endlocation);
        return distance;

    }

    @Override
    public void onSchedule() {
        getTripStatus();
    }

    private void openTripProgressDialog() {
        tripProgressDialog = new Dialog(drawerActivity);
        tripProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tripProgressDialog.setContentView(R.layout.circuler_progerss_bar_two);
        tripProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CustomCircularProgressView ivProgressBar = (CustomCircularProgressView)
                tripProgressDialog.findViewById(R.id
                        .ivProgressBarTwo);
        ivProgressBar.startAnimation();
        tripProgressDialog.setCancelable(false);
        WindowManager.LayoutParams params = tripProgressDialog.getWindow().getAttributes();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        tripProgressDialog.getWindow().setAttributes(params);
        tripProgressDialog.getWindow().setDimAmount(0);
        tripProgressDialog.show();
    }

    private void hideTripProgressDialog() {
        if (tripProgressDialog != null && tripProgressDialog.isShowing()) {
            tripProgressDialog.hide();
        }

    }

    private void setAccurateLocationFilter() {
        drawerActivity.locationHelper.getLastLocation(drawerActivity, new
                OnSuccessListener<Location>() {


                    @Override
                    public void onSuccess(Location location) {
                        drawerActivity.currentLocation = location;
                        drawerActivity.locationFilter(drawerActivity.currentLocation);
                        setCurrentLatLag(drawerActivity.currentLocation);
                    }
                });
    }

    private void callUserViaTwilio() {
        Utils.showCustomProgressDialog(drawerActivity, "", false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId());
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER);
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .twilioCall(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    Utils.hideCustomProgressDialog();
                    openWaitForCallAssignDialog();
                    btnCallCustomer.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnCallCustomer.setEnabled(true);
                        }
                    }, 5000);
                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    Utils.hideCustomProgressDialog();
                    AppLog.handleThrowable(TripFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_FRAGMENT, e);
        }
    }

    private void openWaitForCallAssignDialog() {
        CustomDialogNotification customDialogNotification = new CustomDialogNotification
                (drawerActivity, drawerActivity.getResources().getString(R.string
                        .text_call_message)) {
            @Override
            public void doWithClose() {
                dismiss();
            }
        };
        customDialogNotification.show();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        AppLog.Log(TripFragment.class.getCanonicalName(),
                "message = " + dataSnapshot.getChildrenCount());
        int visible = View.GONE;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Message chatMessage = snapshot.getValue(Message.class);
            if (chatMessage != null) {
                if (!chatMessage.isIs_read() && chatMessage.getType() == Const.USER_UNIQUE_NUMBER) {
                    visible = View.VISIBLE;
                    break;
                }
            }
        }
        ivHaveMessage.setVisibility(visible);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public void onSocketConnect() {
        receiveTripStatusUsingSocket(true);
    }


    @Override
    public void onSocketDisconnect() {
        receiveTripStatusUsingSocket(false);
    }

    @Override
    public void onNetwork(boolean isConnected) {
        if (isConnected) {
            getTripStatus();
        } else {
            Utils.hideCustomProgressDialog();
        }

    }


    /**
     * This Receiver receive tripStatus
     */
    private class TripStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!drawerActivity.isFinishing() && isAdded()) {
                switch (intent.getAction()) {
                    case Const.ACTION_CANCEL_TRIP:
                        stopCountDownTimer();
                        drawerActivity.openUserCancelTripDialog();
                        drawerActivity.goToMapFragment();
                        break;
                    case Const.ACTION_DESTINATION_UPDATE:
                        getTripStatus();
                        break;
                    case Const.ACTION_PAYMENT_CARD:
                        tvPaymentMode.setText(drawerActivity.getResources().getString(R
                                .string.text_card));
                        break;
                    case Const.ACTION_PAYMENT_CASH:
                        tvPaymentMode.setText(drawerActivity.getResources().getString(R
                                .string.text_cash));
                        break;
                    case Const.ACTION_PROVIDER_TRIP_END:
                        drawerActivity.closedProgressDialog();
                        drawerActivity.goToInvoiceFragment();
                        break;
                    case Const.ACTION_TRIP_ACCEPTED_BY_ANOTHER_PROVIDER:
                        stopCountDownTimer();
                        drawerActivity.goToMapFragment();
                        break;
                    default:
                        // do with default
                        break;

                }
            }
        }
    }

    private void goToChatActivity() {
        ivHaveMessage.setVisibility(View.GONE);
        Intent intent = new Intent(drawerActivity, ChatActivity.class);
        startActivity(intent);
        drawerActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private boolean isCarRentalType() {
        return !TextUtils.isEmpty(trip.getCarRentalId());
    }


    private void initFirebaseChat() {
        if (!TextUtils.isEmpty(drawerActivity.preferenceHelper.getTripId())) {
            databaseReference =
                    FirebaseDatabase.getInstance().getReference().child(drawerActivity.preferenceHelper.getTripId());
        }
    }

    private void openRentalPackageDialog() {
        if (tripStatusResponse != null && !drawerActivity.isFinishing()) {
            CityType tripService = tripStatusResponse.getTripService();
            final Dialog dialog = new Dialog(drawerActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_rental_packages);
            TextView tvPackageName, tvPackageInfo, tvPackageUnitPrice, tvPackagePrice;
            tvPackageName = dialog.findViewById(R.id.tvPackageName);
            tvPackageInfo = dialog.findViewById(R.id.tvPackageInfo);
            tvPackageUnitPrice = dialog.findViewById(R.id.tvPackageUnitPrice);
            tvPackagePrice = dialog.findViewById(R.id.tvPackagePrice);
            dialog.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            String basePrice = currencyFormat.format(tripService.getBasePrice());
            String baseTimeAndDistance = ParseContent.getInstance().twoDigitDecimalFormat
                    .format(tripService.getBasePriceTime()) + drawerActivity.getResources()
                    .getString(R.string.text_unit_mins) + " & " + ParseContent.getInstance()
                    .twoDigitDecimalFormat
                    .format(tripService.getBasePriceDistance()) + Utils.showUnit(drawerActivity,
                    trip.getUnit());
            String extraTimePrice = currencyFormat
                    .format(tripService.getPriceForTotalTime()) + drawerActivity.getResources()
                    .getString(R.string.text_unit_per_min);
            String extraDistancePrice = currencyFormat
                    .format(tripService.getPricePerUnitDistance()) + "/" + Utils.showUnit(drawerActivity,
                    trip.getUnit());
            String packageInfo = drawerActivity.getResources().getString(R.string
                    .msg_for_extra_charge, extraDistancePrice, extraTimePrice);

            tvPackageName.setText(tripService.getTypename());
            tvPackagePrice.setText(basePrice);
            tvPackageUnitPrice.setText(baseTimeAndDistance);
            tvPackageInfo.setText(packageInfo);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }

    }

    private void restartLocationServiceIfReburied() {
        ivTipTargetLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                drawerActivity.stopLocationUpdateService();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerActivity.startLocationUpdateService();
                    }
                }, 2000);
                return true;
            }
        });
    }

    /**
     * method used to register and unregister onTripDetail socket listener
     *
     * @param isRegister
     */

    private void receiveTripStatusUsingSocket(boolean isRegister) {
        if (isAdded() && socketHelper != null && socketHelper.isConnected() && !TextUtils.isEmpty(drawerActivity.preferenceHelper.getTripId())) {
            String tripId = String.format("'%s'", drawerActivity.preferenceHelper.getTripId());
            if (isRegister) {
                socketHelper.getSocket().on(tripId, onTripDetail);
            } else {
                socketHelper.getSocket().off(tripId,
                        onTripDetail);
            }
        }
    }

    private void emitTripStatusUsingSocket() {
        if (socketHelper != null && socketHelper.isConnected()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                        .getProviderId());
                jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
                jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                        .getSessionToken());
                jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                        ());
                jsonObject.put(Const.Params.DEVICE_TYPE, "Android");
                socketHelper.getSocket().emit(SocketHelper.TRIP_DETAIL_NOTIFY, jsonObject);
            } catch (JSONException e) {
                AppLog.handleException(TripFragment.class.getSimpleName(), e);
            }
        }
    }
    /*   */
    /**
     * method used to join user with trip using socket
     *//*
    private void joinWithTripUsingSocket() {
        if (socketHelper != null && socketHelper.isConnected() && trip != null && trip
        .getIsProviderAccepted() == Const.TRUE && !TextUtils.isEmpty(trip.getId())) {
            AppLog.Log(TripFragment.class.getSimpleName(), "Socket join with trip");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.TRIP_ID, trip.getId());
                jsonObject.put(Const.Params.TYPE, Const.PROVIDER_UNIQUE_NUMBER);
                jsonObject.put(Const.Params.PROVIDER_ID,
                        drawerActivity.preferenceHelper.getProviderId());
                jsonObject.put(Const.Params.DEVICE_TYPE, "Android");
                socketHelper.getSocket().emit(SocketHelper.JOIN_TRIP, jsonObject);
            } catch (JSONException e) {
                AppLog.handleException(TripFragment.class.getSimpleName(), e);
            }
        }


    }*/

    private Emitter.Listener onTripDetail = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args != null) {
                final JSONObject jsonObject = (JSONObject) args[0];
                final TripDetailOnSocket tripDetailOnSocket =
                        ApiClient.getGsonInstance().fromJson(jsonObject.toString(),
                                TripDetailOnSocket.class);
                AppLog.Log("onTripDetail", jsonObject.toString());


                drawerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (tripDetailOnSocket.isTripUpdated()) {
                            getTripStatus();
                        }

                    }
                });
            }
        }
    };

    private void checkIsTripAccepted(Trip trip) {
        if (trip.getIsProviderAccepted() == Const.TRUE) {
            drawerActivity.stopTripStatusScheduled();
        } else {
            drawerActivity.startTripStatusScheduled();
        }

    }

    private void setTotalDistance(double distance) {
        tvEstimateDistance.setText(String.format("%s %s",
                drawerActivity.parseContent.twoDigitDecimalFormat.format
                        (distance), unit));
    }

    private void setTotalTime(double time) {
        tvEstimateTime.setText(String.format("%s %s",
                drawerActivity.parseContent.timeDecimalFormat.format(time),
                drawerActivity.getResources().getString(R.string
                        .text_unit_mins)));
    }

    public void providerLocationUpdateAtTripStartPoint() {
        sqLiteHelper.clearLocationTable();
        drawerActivity.locationHelper.getLastLocation(drawerActivity,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        drawerActivity.currentLocation = location;
                        if (location != null) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put(Const.Params.PROVIDER_ID,
                                        drawerActivity.preferenceHelper
                                                .getProviderId());
                                jsonObject.put(Const.Params.TOKEN,
                                        drawerActivity.preferenceHelper.getSessionToken());
                                jsonObject.put(Const.Params.LATITUDE, String.valueOf(location
                                        .getLatitude()));
                                jsonObject.put(Const.Params.LONGITUDE, String.valueOf(location
                                        .getLongitude()));
                                jsonObject.put(Const.Params.BEARING, 0);
                                jsonObject.put(Const.Params.TRIP_ID,
                                        drawerActivity.preferenceHelper.getTripId());
                                jsonObject.put(Const.Params.LOCATION_UNIQUE_ID,
                                        drawerActivity.preferenceHelper
                                                .getIsHaveTrip()
                                                ?
                                                drawerActivity.preferenceHelper.getLocationUniqueId() :
                                                0);
                                if (Utils.isInternetConnected(drawerActivity)) {
                                    sqLiteHelper.addLocation(String.valueOf(location
                                                    .getLatitude()),
                                            String.valueOf(location.getLongitude()),
                                            drawerActivity.preferenceHelper
                                                    .getLocationUniqueId());
                                    jsonObject.put(Const.google.LOCATION, sqLiteHelper
                                            .getAllDBLocations());
                                    updateLocationUsingSocket(jsonObject);
                                }

                            } catch (JSONException e) {
                                AppLog.handleException(TAG, e);
                            }
                        }

                    }
                });


    }

    /**
     * emit provider location using socket
     *
     * @param jsonObject
     */
    private void updateLocationUsingSocket(JSONObject jsonObject) {
        if (socketHelper != null && socketHelper.isConnected()) {
            socketHelper.getSocket().emit(SocketHelper.UPDATE_LOCATION, jsonObject);
        }

    }
}



