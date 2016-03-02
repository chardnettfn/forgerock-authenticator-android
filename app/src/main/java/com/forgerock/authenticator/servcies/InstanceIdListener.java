package com.forgerock.authenticator.servcies;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by ken.stubbings on 26/02/2016.
 */
public class InstanceIdListener extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, InstanceIdListener.class);
        startService(intent);
    }
}