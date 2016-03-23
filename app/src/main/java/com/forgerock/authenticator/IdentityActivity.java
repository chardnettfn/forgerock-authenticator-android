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

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;

import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.identity.IdentityAdapter;

import roboguice.activity.RoboActivity;

/**
 * Page for viewing a list of all Identities. Currently the start page for the app.
 */
public class IdentityActivity extends RoboActivity implements MenuItem.OnMenuItemClickListener {

    private IdentityAdapter identityAdapter;
    private DataSetObserver dataSetObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        identityAdapter.notifyDataSetChanged();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_scan).setVisible(ScanActivity.haveCamera());
        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                startActivity(new Intent(this, ScanActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                return true;
        }
        return false;
    }
}
