package com.elluminati.eber.driver.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by elluminati on 20-06-2016.
 */
public class LocationHelper implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final long INTERVAL = 5000;// millisecond
    private static final long FASTEST_INTERVAL = 3000;// millisecond
    private static final float DISPLACEMENT = 5; //meter
    private final String Tag = "LOCATION_HELPER";
    private Context context;
    private OnLocationReceived locationReceived;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isStarted = false;
    private SettingsClient settingsClient;
    private LocationSettingsRequest.Builder locationSettingBuilder;
    private Location currentLocation;

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        setLocationCallback();
        getLocationRequest();
        buildLocationSettingsRequest();
    }

    private void getLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                AppLog.Log("Current Location", currentLocation.getLatitude() + " , " +
                        "" + currentLocation.getLongitude());
                locationReceived.onLocationChanged(currentLocation);
            }
        };
    }

    private void buildLocationSettingsRequest() {
        locationSettingBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settingsClient = LocationServices.getSettingsClient(context);
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback, null);
        isStarted = true;
    }

    private void stopLocationUpdate() {
        if (isStarted)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            AppLog.Log(Tag, "Stop location update");
                        }
                    });
    }

    public void setLocationReceivedLister(OnLocationReceived locationReceived) {
        this.locationReceived = locationReceived;
    }

    public void getLastLocation(Context context, OnSuccessListener<Location> onSuccessListener) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(onSuccessListener);
    }

    public void onStart() {
        if (!isStarted)
            startLocationUpdate();
    }

    public void onStop() {
        stopLocationUpdate();
        isStarted = false;
    }

    public void checkLocationSetting(final Activity activity) {
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings
                (locationSettingBuilder.build());
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity,
                                    Const.LOCATION_SETTING_REQUEST);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        AppLog.Log(Tag, "GoogleApiClient is Connected Successfully");
        locationReceived.onConnected(bundle);

    }

    @Override
    public void onConnectionSuspended(int i) {
        AppLog.Log(Tag, "GoogleApiClient is Connection Suspended ");

        locationReceived.onConnectionSuspended(i);

    }

    @Override
    public void onLocationChanged(Location location) {
        AppLog.Log(Tag, "Location Changed");
        currentLocation = location;
        if (currentLocation != null) {
            locationReceived.onLocationChanged(currentLocation);
        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AppLog.Log(Tag, "GoogleApiClient is Failed to Connect ");
        locationReceived.onConnectionFailed(connectionResult);

    }


    public interface OnLocationReceived {

        void onLocationChanged(Location location);


        public void onConnected(Bundle bundle);

        public void onConnectionFailed(@NonNull ConnectionResult connectionResult);

        public void onConnectionSuspended(int i);


    }
}