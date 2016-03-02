package com.forgerock.authenticator.servcies;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.forgerock.authenticator.MainActivity;
import com.forgerock.authenticator.R;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by ken.stubbings on 25/02/2016.
 */
public class GCMServcie extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        handleMessage(message);
    }

    private void handleMessage(String message) {

        // TODO: change activity to show one that shows either the emssage or a list of "unread" messages
        Intent intent = new Intent(this, MainActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.forgerock_notification)
                .setContentTitle("Login Detected")
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setContentIntent(pendIntent)
                .build();

        NotificationManager noteMan =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noteMan.notify(0, notification);

    }

}
