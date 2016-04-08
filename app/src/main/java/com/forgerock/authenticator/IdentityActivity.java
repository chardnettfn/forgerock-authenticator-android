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

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;

import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.baseactivities.BaseActivity;
import com.forgerock.authenticator.identity.IdentityAdapter;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.storage.IdentityModelListener;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Page for viewing a list of all Identities. Currently the start page for the app.
 */
public class IdentityActivity extends BaseActivity {

    private IdentityAdapter identityAdapter;
    private DataSetObserver dataSetObserver;
    private IdentityModelListener listener;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("My Accounts");
            actionBar.setDisplayUseLogoEnabled(false);
        }

        onNewIntent(getIntent());
        setContentView(R.layout.identity);

        // Don't permit screenshots since these might contain secret information.
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_scan).setVisible(settings.isCameraEnabled());

        final Context context = this;

        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(context, ScanActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                return true;
            }
        });

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(context, SettingsActivity.class));
                return true;
            }
        });
        return true;
    }
}
