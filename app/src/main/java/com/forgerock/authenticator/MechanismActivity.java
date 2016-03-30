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

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;

import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismAdapter;
import com.forgerock.authenticator.storage.IdentityDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 *  * Page for viewing a list of all Identities. Requires IDENTITY_ID to be set in the Intent.
 */
public class MechanismActivity extends RoboActivity implements OnMenuItemClickListener {

    /** The identity of the ID that this page displays the mechanisms for. */
    public static final String IDENTITY_ID = "identityid";

    private final Logger logger;
    private MechanismAdapter mechanismAdapter;
    private DataSetObserver dataSetObserver;

    /**
     * Default instance of MainActivity will be created by Android framework.
     */
    public MechanismActivity() {
        this(LoggerFactory.getLogger(MechanismActivity.class));
    }

    /**
     * Dependencies exposed for unit testing as required.
     *
     * @param logger Non null logging instance.
     */
    @VisibleForTesting
    public MechanismActivity(final Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long identityId = getIntent().getLongExtra(IDENTITY_ID, -1);
        assert identityId >= 0;

        Identity identity = RoboGuice.getInjector(this).getInstance(IdentityDatabase.class).getIdentity(identityId);
        assert identity != null;

        onNewIntent(getIntent());

        setContentView(R.layout.mechanism);

        mechanismAdapter = new MechanismAdapter(this, identity);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mechanismAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
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
