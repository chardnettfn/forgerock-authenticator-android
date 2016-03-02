package com.forgerock.authenticator.servcies;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.forgerock.authenticator.AboutActivity;
import com.forgerock.authenticator.MainActivity;
import com.forgerock.authenticator.R;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by ken.stubbings on 01/03/2016.
 */
public class GCMRegistrationService extends IntentService {
    private static final String TAG = "RegIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public GCMRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            String token = getToken();

            Log.i(TAG, "GCM Registration Token: " + token);

            GcmPubSub.getInstance(this).subscribe(token, "/topics/global", null);

            sharedPreferences.edit().putBoolean(MessageConstants.TOKEN_SENT_TO_SERVER, true).apply();

            handleToken(token);

        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(MessageConstants.TOKEN_SENT_TO_SERVER, false).apply();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageConstants.REGISTRATION_COMPLETE));
    }

    private String getToken() throws IOException {
        InstanceID instanceId = InstanceID.getInstance(this);
        return instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }

    private void handleToken(String token) {

        // TODO: change activity to one that shoes the current device message token
        Intent intent = new Intent(this, AboutActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.forgerock_notification)
                .setContentTitle("New Device Message token received:")
                .setContentText(token)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setContentIntent(pendIntent)
                .build();

        NotificationManager noteMan =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noteMan.notify(5, notification);

    }
}
