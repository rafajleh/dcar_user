package com.elluminati.eber.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by elluminati on 30-03-2016.
 * <p>
 * This Class is handle a Notification which send by Google FCM server.
 */
public class FcmMessagingService extends FirebaseMessagingService {

    public static final String MESSAGE = "message";
    public static final String PROVIDER_DECLINE = "208";
    public static final String PROVIDER_APPROVED = "207";
    public static final String PROVIDER_HAVE_NEW_TRIP = "201";
    public static final String USER_CANCEL_TRIP = "205";
    public static final String USER_DESTINATION_UPDATE = "210";
    public static final String PAYMENT_CASH = "211";
    public static final String PAYMENT_CARD = "212";
    public static final String LOG_OUT = "230";
    public static final String PROVIDER_TRIP_END = "241";
    public static final String PROVIDER_OFFLINE = "242";
    public static final String TRIP_ACCEPTED_BY_ANOTHER_PROVIDER = "232";
    private static final String CHANNEL_ID = "channel_01";
    private static final String PROFILE_APPROVED = "233";
    private static final String VIHICLE_APPROVED = "234";
    private static final String DOCUMENT_APPROVED = "235";
    private LocalBroadcastManager localBroadcastManager;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        if (remoteMessage != null) {

            AppLog.Log(Const.Tag.FCM_MESSAGING_SERVICE, "From:" + remoteMessage.getFrom());
            AppLog.Log(Const.Tag.FCM_MESSAGING_SERVICE, "Data:" + remoteMessage.getData());


            String message = remoteMessage.getData().get(MESSAGE);
            if (message != null && !message.isEmpty()) {
                tripStatus(message);
                AppLog.Log("onMessageReceived", message);
            }

        }
    }

    private void sendNotification(String message) {


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainDrawerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent
                .FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainDrawerActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder notificationBuilder = new Notification.Builder
                (this).setPriority(Notification.PRIORITY_MAX).setContentTitle(this.getResources().getString(R.string
                .app_name)).setContentText(message).setAutoCancel(true).setSmallIcon(getNotificationIcon()).setContentIntent(notificationPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID); // Channel ID
        }

        if (PreferenceHelper.getInstance(this).getIsPushNotificationSoundOn()) {
            notificationBuilder.setDefaults(Notification.DEFAULT_SOUND
                    | Notification.DEFAULT_LIGHTS);
        }
        notificationManager.notify(Const.PUSH_NOTIFICATION_ID, notificationBuilder
                .build());
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build
                .VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_eber : R.mipmap.ic_launcher;
    }

    private void tripStatus(String status) {
        switch (status) {
            case PROVIDER_APPROVED:
                sendNotification(getMessage(status));
                sendGlobalBroadcast(Const.ACTION_APPROVED_PROVIDER);
                break;
            case PROVIDER_DECLINE:
                sendNotification(getMessage(status));
                sendGlobalBroadcast(Const.ACTION_DECLINE_PROVIDER);
                break;
            case USER_CANCEL_TRIP:
                sendNotification(getMessage(status));
                sendLocalBroadcast(Const.ACTION_CANCEL_TRIP);
                break;
            case PROVIDER_HAVE_NEW_TRIP:
                sendNotification(getMessage(status));
                sendBroadcast(new Intent(Const.ACTION_NEW_TRIP));
                break;
            case USER_DESTINATION_UPDATE:
                sendNotification(getMessage(status));
                sendLocalBroadcast(Const.ACTION_DESTINATION_UPDATE);
                break;
            case PAYMENT_CARD:
                sendNotification(getMessage(status));
                sendLocalBroadcast(Const.ACTION_PAYMENT_CARD);
                break;
            case PAYMENT_CASH:
                sendNotification(getMessage(status));
                sendLocalBroadcast(Const.ACTION_PAYMENT_CASH);
                break;
            case LOG_OUT:
                sendNotification(getMessage(status));
                goToMainActivity();
                break;
            case PROVIDER_TRIP_END:
                sendLocalBroadcast(Const.ACTION_PROVIDER_TRIP_END);
                sendNotification(getMessage(status));
                break;
            case PROVIDER_OFFLINE:
                sendNotification(getMessage(status));
                sendLocalBroadcast(Const.ACTION_PROVIDER_OFFLINE);
                break;
            case TRIP_ACCEPTED_BY_ANOTHER_PROVIDER:
                sendLocalBroadcast(Const.ACTION_TRIP_ACCEPTED_BY_ANOTHER_PROVIDER);
                sendNotification(getMessage(status));
                break;

            case PROFILE_APPROVED:
                sendLocalBroadcast(Const.PROFILE_APPROVED_ACTION);
                sendNotification(getMessage(status));
                sendGlobalBroadcast(Const.PROFILE_APPROVED_ACTION);
                break;

            case VIHICLE_APPROVED:
                sendLocalBroadcast(Const.VIHICLE_APPROVED_ACTION);
                sendNotification(getMessage(status));
                sendGlobalBroadcast(Const.VIHICLE_APPROVED_ACTION);
                break;

            case DOCUMENT_APPROVED:
                sendLocalBroadcast(Const.DOCUMENT_APPROVED_ACTION);
                sendNotification(getMessage(status));
                sendGlobalBroadcast(Const.DOCUMENT_APPROVED_ACTION);
                break;


            default:
                sendNotification(status);
                break;
        }
    }

    private String getMessage(String code) {
        String msg = "";
        String messageCode = Const.PUSH_MESSAGE_PREFIX + code;
        msg = this.getResources().getString(this.getResources().getIdentifier(messageCode, Const
                .STRING, this.getPackageName()));
        return msg;
    }

    private void sendLocalBroadcast(String action) {
        Intent intent = new Intent(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendGlobalBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void goToMainActivity() {
        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(this);
        preferenceHelper.logout();
        Intent sigInIntent = new Intent(this, MainActivity.class);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sigInIntent);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        AppLog.Log(FcmMessagingService.class.getSimpleName(), "FCM Token Refresh = " + token);
        PreferenceHelper.getInstance(this).putDeviceToken(token);
        if (PreferenceHelper.getInstance(this).getSessionToken() != null) {
            updateDeviceTokenOnServer(token);
        }
    }


    private void updateDeviceTokenOnServer(String deviceToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate(Const.Params.TOKEN, PreferenceHelper.getInstance(this)
                    .getSessionToken());
            jsonObject.accumulate(Const.Params.PROVIDER_ID, PreferenceHelper.getInstance(this)
                    .getProviderId());
            jsonObject.accumulate(Const.Params.DEVICE_TOKEN, deviceToken);

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .updateDeviceToken(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Utils.showMessageToast(response.body().getMessage(),
                                    FcmMessagingService.this);
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(),
                                    FcmMessagingService.this);
                        }
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(FcmMessagingService.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException("FCM Token Refresh", e);
        }
    }

}