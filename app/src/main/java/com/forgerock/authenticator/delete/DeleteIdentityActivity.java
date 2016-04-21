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

package com.forgerock.authenticator.delete;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseIdentityActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.IdentityModel;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Activity responsible for verifying that a user wishes to delete an identity, then deleting it.
 */
public class DeleteIdentityActivity extends BaseIdentityActivity {

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = LoggerFactory.getLogger(DeleteIdentityActivity.class);

        setContentView(R.layout.delete);

        final Identity identity = getIdentity();
        if (identity == null) {
            logger.error("Failed to find Identity to delete.");
            finish();
            return;
        }
        ((TextView) findViewById(R.id.issuer)).setText(identity.getIssuer());
        ((TextView) findViewById(R.id.label)).setText(identity.getAccountName());
        Picasso.with(this)
                .load(identity.getImage())
                .placeholder(R.drawable.forgerock_placeholder)
                .into((ImageView) findViewById(R.id.image));

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identityModel.removeIdentity(identity);
                finish();
            }
        });
    }
}
