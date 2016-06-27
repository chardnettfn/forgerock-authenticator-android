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
 * Copyright 2016 ForgeRock AS.
 */

package com.forgerock.authenticator;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.forgerock.authenticator.add.CreateMechanismFromUriTask;
import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.baseactivities.BaseActivity;
import com.forgerock.authenticator.identity.IdentityAdapter;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.DuplicateMechanismException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.storage.IdentityModelListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Page for viewing a list of all Identities. Currently the start page for the app.
 */
public class IdentityActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private IdentityAdapter identityAdapter;
    private DataSetObserver dataSetObserver;
    private IdentityModelListener listener;
    private Menu menu;
    private final int PERMISSION_REQUEST_SCAN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.identity_title);
            actionBar.setDisplayUseLogoEnabled(false);
        }

        onNewIntent(getIntent());
        setContentView(R.layout.identity);

        identityAdapter = new IdentityAdapter(this);
        final GridView identityView = ((GridView) findViewById(R.id.grid));
        identityView.setAdapter(identityAdapter);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (identityAdapter.getCount() == 0) {
                    findViewById(R.id.empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.empty).setVisibility(View.GONE);
                }
            }
        };
        identityAdapter.registerDataSetObserver(dataSetObserver);

        listener = new IdentityModelListener() {
            @Override
            public void notificationChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        identityAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        identityModel.addListener(listener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        identityAdapter.notifyDataSetChanged();

        if (menu != null) {
            menu.findItem(R.id.action_scan).setVisible(settings.isCameraEnabled());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        identityAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        identityAdapter.unregisterDataSetObserver(dataSetObserver);
        identityModel.removeListener(listener);
    }

    @VisibleForTesting
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_scan).setVisible(settings.isCameraEnabled());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_SCAN);
                } else {
                    startActivity(new Intent(this, ScanActivity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            new CreateMechanismFromUriTask(this, null).execute(uri.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_SCAN && permissions.length > 0 && Manifest.permission.CAMERA.equals(permissions[0])) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                startActivity(new Intent(this, ScanActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        }
    }
}
