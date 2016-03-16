/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Portions Copyright 2009 ZXing authors
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.Toast;

import com.forgerock.authenticator.add.AddActivity;
import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismAdapter;
import com.forgerock.authenticator.message.GcmRegistrationService;
import com.forgerock.authenticator.message.MessageConstants;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.utils.TestNGCheck;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * The main entry point for the Authenticator App.
 *
 * Responsible for handling all startup concerns for the app. Key details:
 *
 * <li>check that all required services are present</li>
 * <li>prevent screenshots of the app</li>
 * <li>show the first screen to the user</li>
 */
public class MainActivity extends RoboActivity implements OnMenuItemClickListener {
    private final Logger logger;

    private MechanismAdapter mechanismAdapter;
    private DataSetObserver dataSetObserver;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Default instance of MainActivity will be created by Android framework.
     */
    public MainActivity() {
        this(LoggerFactory.getLogger(MainActivity.class));
    }

    /**
     * Dependencies exposed for unit testing as required.
     * 
     * @param logger Non null logging instance.
     */
    @VisibleForTesting
    public MainActivity(final Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onNewIntent(getIntent());
        setContentView(R.layout.main);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };

        mechanismAdapter = new MechanismAdapter(this, new Identity());
        ((GridView) findViewById(R.id.grid)).setAdapter(mechanismAdapter);

        // Don't permit screenshots since these might contain OTP codes.
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mechanismAdapter.getCount() == 0) {
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(android.R.id.empty).setVisibility(View.GONE);
                }
            }
        };
        mechanismAdapter.registerDataSetObserver(dataSetObserver);


        // // TODO: AME-9928 check should be performed in on-resume as well.
        if (checkPlayServices()) {
            startService(new Intent(this, GcmRegistrationService.class));
        }

        // Notify developer that they have included TestNG on classpath.
        // TODO: AME-9927 should update or resolve this check
        if (TestNGCheck.isTestNGOnClassPath()) {
            Toast.makeText(getApplicationContext(), "Compiled with test libraries", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(MessageConstants.REGISTRATION_COMPLETE));
        mechanismAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
        mechanismAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mechanismAdapter.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_scan).setVisible(ScanActivity.haveCamera());
        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(this);
        menu.findItem(R.id.action_add).setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_scan:
            startActivity(new Intent(this, ScanActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            return true;

        case R.id.action_add:
            startActivity(new Intent(this, AddActivity.class));
            return true;
        }

        return false;
    }

    // TODO: AME-9928 should seek to upgrade this functionality
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }
}
