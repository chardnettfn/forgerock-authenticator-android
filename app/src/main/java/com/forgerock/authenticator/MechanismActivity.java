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
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.baseactivities.BaseIdentityActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismAdapter;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.storage.IdentityModelListener;
import com.squareup.picasso.Picasso;

import roboguice.RoboGuice;

/**
 * Page for viewing the mechanisms relating to an identity.
 */
public class MechanismActivity extends BaseIdentityActivity {

    private MechanismAdapter mechanismAdapter;
    private IdentityModelListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mechanism);

        final Identity identity = getIdentity();
        if (identity == null) {
            finish();
            return;
        }

        TextView issuerView = (TextView) findViewById(R.id.issuer);
        issuerView.setText(identity.getIssuer());
        TextView accountNameView = (TextView) findViewById(R.id.account_name);
        accountNameView.setText(identity.getAccountName());

        mechanismAdapter = new MechanismAdapter(this, identity);

        ((GridView) findViewById(R.id.grid)).setAdapter(mechanismAdapter);

        try {
            String color = identity.getBackgroundColor();
            if (color != null) {
                findViewById(R.id.header).setBackgroundColor(Color.parseColor(color));
            }
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        ImageView imageView = (ImageView) findViewById(R.id.image);

        Picasso.with(this)
                .load(identity.getImageURL())
                .placeholder(R.drawable.forgerock_placeholder)
                .into(imageView);

        listener = new IdentityModelListener() {
            @Override
            public void notificationChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mechanismAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        identityModel.addListener(listener);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.mechanism_title);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mechanismAdapter.notifyDataSetChanged();
        if (getIdentity().getMechanisms().size() == 0) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mechanismAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        identityModel.removeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
