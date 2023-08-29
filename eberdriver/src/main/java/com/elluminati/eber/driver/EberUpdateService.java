package com.elluminati.eber.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;

import com.elluminati.eber.driver.models.responsemodels.ProviderLocationResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.KalmanLatLong;
import com.elluminati.eber.driver.utils.LocationHelper;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.SQLiteHelper;
import com.elluminati.eber.driver.utils.SocketHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ScheduledExecutorService;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by elluminati on 24-06-2016.
 */
public class EberUpdateService extends Service implements LocationHelper
        .OnLocationReceived {
    public String TAG = EberUpdateService.class.getSimpleName();
    private static final int PERIOD = 5000;
    private static final String CHANNEL_ID = "channel_01";
    private final LocalBinder localBinder = new LocalBinder();
    private ScheduledExecutorService tripStatusSchedule;
    private PreferenceHelper preferenceHelper;
    private ParseContent parseContent;
    private LocationHelper locationHelper;
    private Location currentLocation;
    private Location lastLocation;
    private boolean isScheduledStart;
    private int countUpdateForLocation = 0;
    private ServiceReceiver serviceReceiver;
    private SQLiteHelper sqLiteHelper;
    private KalmanLatLong kalmanLatLong;
    private int DISTANCE_DISPLACEMENT = 3;// kilometer
    private SocketHelper socketHelper;
    //private IncomingHandler incomingHandler;
    private boolean isLocationUpdate;
    private int startId;

    @Override
    public void onCreate() {
        super.onCreate();
        sqLiteHelper = new SQLiteHelper(this);
        locationHelper = new LocationHelper(this);
        locationHelper.setLocationReceivedLister(this);
        preferenceHelper = PreferenceHelper.getInstance(this);
        parseContent = ParseContent.getInstance();
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_ACCEPT_NOTIFICATION);
        intentFilter.addAction(Const.ACTION_CANCEL_NOTIFICATION);
        registerReceiver(serviceReceiver, intentFilter);
        kalmanLatLong = new KalmanLatLong(25);
//        incomingHandler = new IncomingHandler(this);
        socketHelper = SocketHelper.getInstance();
        socketHelper.getSocket().on(Socket.EVENT_CONNECT, onConnect);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startId = startId;
        if (intent != null && intent.getAction() != null) {

            if (intent.getAction().equals(Const.Action.STARTFOREGROUND_ACTION)) {
                startForeground(Const.FOREGROUND_NOTIFICATION_ID,
                        getNotification(getResources().getString(R.string.app_name)));
            } else if (intent.getAction().equals(
                    Const.Action.STOPFOREGROUND_ACTION)) {
                AppLog.Log(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf(startId);
            }
        }
        checkPermission();
        socketHelper.socketConnect();
        countUpdateForLocation = preferenceHelper.getCheckCountForLocation();
        return START_STICKY;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // to do
        } else {
            //Do the stuff that requires permission...
            locationHelper.onStart();
            locationHelper.getLastLocation(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    currentLocation = location;
                    locationFilter(currentLocation);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
        preferenceHelper.putCheckCountForLocation(countUpdateForLocation);
        AppLog.Log(TAG, "ServiceStop");
        //stopTripStatusScheduled();
        locationHelper.onStop();
        socketHelper.socketDisconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

   /* private void startTripStatusScheduled() {

        if (!isScheduledStart) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Message message = incomingHandler.obtainMessage();
                    incomingHandler.sendMessage(message);
                }
            };
            tripStatusSchedule = Executors.newSingleThreadScheduledExecutor();
            tripStatusSchedule.scheduleWithFixedDelay(runnable, 0, Const.SCHEDULED_SECONDS,
                    TimeUnit.SECONDS);
            AppLog.Log(TAG, "Schedule Start");
            isScheduledStart = true;
        }
    }

    private void stopTripStatusScheduled() {
        if (isScheduledStart) {
            AppLog.Log(TAG, "Schedule Stop");
            tripStatusSchedule.shutdown(); // Disable new tasks from being submitted
            // Wait a while for existing tasks to terminate
            try {
                if (!tripStatusSchedule.awaitTermination(60, TimeUnit.SECONDS)) {
                    tripStatusSchedule.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!tripStatusSchedule.awaitTermination(60, TimeUnit.SECONDS))
                        AppLog.Log(TAG, "Pool did not terminate");

                }
            } catch (InterruptedException e) {
                AppLog.handleException(TAG, e);
                // (Re-)Cancel if current thread also interrupted
                tripStatusSchedule.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
            isScheduledStart = false;
        }

    }*/


    private void providerLocationUpdateWhenTrip(String tripId) {
        if (currentLocation != null) {
            float speed = currentLocation.getSpeed() * Const.KM_COEFFICIENT;
            if ((!Float.isNaN(speed) && speed > DISTANCE_DISPLACEMENT) || BuildConfig.DEBUG) {
                AppLog.Log("UPDATE_LOCATION_WHEN_TRIP", currentLocation.getLatitude() + " " +
                        currentLocation
                                .getLongitude() + " " + currentLocation.getBearing());

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
                    jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
                    jsonObject.put(Const.Params.LATITUDE, String.valueOf(currentLocation
                            .getLatitude()));
                    jsonObject.put(Const.Params.LONGITUDE, String.valueOf(currentLocation
                            .getLongitude()));
                    jsonObject.put(Const.Params.BEARING, getBearing(lastLocation,
                            currentLocation));
                    jsonObject.put(Const.Params.TRIP_ID, tripId);
                    jsonObject.put(Const.Params.LOCATION_UNIQUE_ID, preferenceHelper.getIsHaveTrip()
                            ? preferenceHelper.getLocationUniqueId() : 0);
                    if (Utils.isInternetConnected(EberUpdateService.this)) {
                        setLastLocation(currentLocation);
                        sqLiteHelper.addLocation(String.valueOf(currentLocation.getLatitude()),
                                String.valueOf(currentLocation.getLongitude()), preferenceHelper
                                        .getLocationUniqueId());
                        jsonObject.put(Const.google.LOCATION, sqLiteHelper.getAllDBLocations());
                        updateLocationUsingSocket(jsonObject);

                    } else {
                        AppLog.Log(TAG, "currentLocation = " +
                                currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                        AppLog.Log(TAG, "lastLocation = " +
                                lastLocation.getLatitude() + " " + lastLocation.getLongitude());
                        if (!TextUtils.isEmpty(tripId) && !locationMatch(lastLocation,
                                currentLocation)) {
                            sqLiteHelper.addLocation(String.valueOf(currentLocation.getLatitude()),
                                    String.valueOf(currentLocation.getLongitude()),
                                    preferenceHelper.getLocationUniqueId());
                            setLastLocation(currentLocation);
                        }
                    }
                } catch (JSONException e) {
                    AppLog.handleException(TAG, e);
                }
            }
        }

    }


    private void providerPreviousLocationUpdateWhenTrip(String tripId, int id) {
        if (currentLocation != null) {
            AppLog.Log("UPDATE_LOCATION_WHEN_TRIP", currentLocation.getLatitude() + " " +
                    currentLocation
                            .getLongitude() + " " + currentLocation.getBearing());

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
                jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
                jsonObject.put(Const.Params.LATITUDE, String.valueOf(currentLocation
                        .getLatitude()));
                jsonObject.put(Const.Params.LONGITUDE, String.valueOf(currentLocation
                        .getLongitude()));
                jsonObject.put(Const.Params.BEARING, getBearing(lastLocation,
                        currentLocation));
                jsonObject.put(Const.Params.TRIP_ID, tripId);
                jsonObject.put(Const.Params.LOCATION_UNIQUE_ID, id);
                jsonObject.put(Const.google.LOCATION, sqLiteHelper.getDBLocationsFromId(id));
                setLastLocation(currentLocation);
                // update location
                updateLocationUsingSocket(jsonObject);

            } catch (JSONException e) {
                AppLog.handleException(TAG, e);
            }
        }

    }

    private void providerLocationUpdateNoTrip() {
        if (currentLocation != null) {

            AppLog.Log("UPDATE_LOCATION_NO_TRIP", currentLocation.getLatitude() + " " +
                    currentLocation
                            .getLongitude() + " " + currentLocation.getBearing());

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
                jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
                jsonObject.put(Const.Params.LATITUDE, String.valueOf(currentLocation
                        .getLatitude()));
                jsonObject.put(Const.Params.LONGITUDE, String.valueOf(currentLocation
                        .getLongitude()));
                jsonObject.put(Const.Params.BEARING, getBearing(lastLocation,
                        currentLocation));
                jsonObject.put(Const.Params.TRIP_ID, "");
                jsonObject.put(Const.Params.LOCATION_UNIQUE_ID, 0);

                JSONArray location = new JSONArray();
                location.put(currentLocation.getLatitude());
                location.put(currentLocation.getLongitude());
                location.put(System.currentTimeMillis());
                JSONArray locationJSONArray = new JSONArray();
                jsonObject.put(Const.google.LOCATION, locationJSONArray.put(location));
                setLastLocation(currentLocation);
                // update location
                updateLocationUsingSocket(jsonObject);


            } catch (JSONException e) {
                AppLog.handleException(TAG, e);
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        locationFilter(currentLocation);
        if (!isLocationUpdate) {
            updateLocation();
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

    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_eber : R.mipmap.ic_launcher;
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        notificationManager.cancel(Const.SERVICE_NOTIFICATION_ID);
    }


    private float getBearing(Location begin, Location end) {
        float bearing = 0;
        if (begin != null && end != null) {
            bearing = begin.bearingTo(end);
            AppLog.Log(EberUpdateService.class.getName(), "Bearing = " + bearing);
            return bearing;
        } else {
            return 0;
        }
    }

    private void getProviderLocationResponse(ProviderLocationResponse response) {
        if (response.isSuccess()) {
            if (preferenceHelper.getIsHaveTrip()) {
                sqLiteHelper.deleteLocation(response.getLocationUniqueId());
                int uniqueIdForLocation = preferenceHelper.getLocationUniqueId();
                uniqueIdForLocation++;
                preferenceHelper.putLocationUniqueId(uniqueIdForLocation);
            } else {
                sqLiteHelper.clearLocationTable();
            }
            CurrentTrip currentTrip = CurrentTrip.getInstance();
            currentTrip.setTotalDistance(response.getTotalDistance());
            currentTrip.setTotalTime(response.getTotalTime());
            Utils.hideLocationUpdateDialog();
        } else {
      /*      if (preferenceHelper.getIsHaveTrip() && !TextUtils.isEmpty(preferenceHelper
                    .getTripId())) {
                providerPreviousLocationUpdateWhenTrip(preferenceHelper.getTripId(),
                        response.getLocationUniqueId());
            }*/

        }


    }

    /**
     * this method filter fluctuate location
     *
     * @param location
     */
    private void locationFilter(Location location) {
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

    /**
     * this method get Notification object which help to notify user as foreground service
     *
     * @param notificationDetails
     * @return
     */
    private Notification getNotification(String notificationDetails) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainDrawerActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainDrawerActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent exitIntent = new Intent(this, EberUpdateService.class);
        exitIntent.setAction(Const.Action.STOPFOREGROUND_ACTION);
        PendingIntent pexitIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pexitIntent = PendingIntent.getService(this, 0,
                    exitIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pexitIntent = PendingIntent.getService(this, 0,
                    exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);        }

        Notification.Builder builder = new Notification.Builder(this);

        builder.setSmallIcon(getNotificationIcon())
                .setContentTitle(notificationDetails)
                .setContentText(getResources().getString(R.string.msg_service))
                .setContentIntent(notificationPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        builder.setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.addAction(R.drawable.cross_icon,
                    getResources().getString(R.string.text_exit_caps), pexitIntent);
        }
        return builder.build();
    }


    private class ServiceReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Const.ACTION_CANCEL_NOTIFICATION:
                        clearNotification();
                        break;
                }
            }


        }

    }

    public class LocalBinder extends Binder {
        public EberUpdateService getService() {
            return EberUpdateService.this;
        }
    }

    private void setLastLocation(Location location) {
        if (lastLocation == null) {
            lastLocation = new Location("lastLocation");
        }
        lastLocation.set(location);
    }

    private boolean locationMatch(Location lastLocation, Location currentLocation) {
        return lastLocation.getLongitude() == currentLocation.getLongitude() && lastLocation.getLatitude() == currentLocation.getLatitude();

    }

    /**
     * emit provider location using socket
     *
     * @param jsonObject
     */
    private void updateLocationUsingSocket(JSONObject jsonObject) {
        if (socketHelper != null && socketHelper.isConnected()) {
            isLocationUpdate = true;
            socketHelper.getSocket().emit(SocketHelper.UPDATE_LOCATION, jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        JSONObject jsonObject1 = (JSONObject) args[0];
                        ProviderLocationResponse providerLocationResponse =
                                ApiClient.getGsonInstance().fromJson(jsonObject1.toString(),
                                        ProviderLocationResponse.class);
                        getProviderLocationResponse(providerLocationResponse);
                    }
                    isLocationUpdate = false;
                }
            });
        }

    }


    /* */

    /**
     * This handler receive a message from  requestStatusScheduledService and check request
     * on web also provider location update
     *//*
    private static class IncomingHandler extends Handler {

        EberUpdateService eberUpdateService;

        private IncomingHandler(EberUpdateService eberUpdateService) {
            this.eberUpdateService = eberUpdateService;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (eberUpdateService != null) {
                eberUpdateService.onSchedule();
            }

        }
    }*/

  /*  public void onSchedule() {
        AppLog.Log(TAG, "onScheduleCall");
        AppLog.Log(TAG, "Have Tip =" + preferenceHelper.getIsHaveTrip());
        if (preferenceHelper.getSessionToken() != null) {
            if (currentLocation != null) {
                if (preferenceHelper.getIsHaveTrip()) {
                    providerLocationUpdateWhenTrip(preferenceHelper.getTripId());
                    isJoinTrip = true;
                } else {
                    isJoinTrip = false;
                    if (preferenceHelper.getIsProviderOnline() ==
                            Const.ProviderStatus.PROVIDER_STATUS_ONLINE) {
                        providerLocationUpdateNoTrip();
                    } else {
                        stopForeground(true);
                        stopSelf(startId);
                    }
                }
            }
        } else {
            stopForeground(true);
            stopSelf(startId);
        }
    }*/
    private void updateLocation() {
        if (preferenceHelper.getSessionToken() != null) {
            if (currentLocation != null) {
                if (preferenceHelper.getIsHaveTrip()) {
                    providerLocationUpdateWhenTrip(preferenceHelper.getTripId());
                } else {
                    if (preferenceHelper.getIsProviderOnline() ==
                            Const.ProviderStatus.PROVIDER_STATUS_ONLINE) {
                        providerLocationUpdateNoTrip();
                    } else {
                        stopForeground(true);
                        stopSelf(startId);
                    }
                }
            }
        } else {
            stopForeground(true);
            stopSelf(startId);
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            updateLocation();
        }
    };
}
