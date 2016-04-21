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
 */

package com.forgerock.authenticator.delete;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseMechanismActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityModel;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Activity responsible for verifying that a user wishes to delete a mechanism, then deleting it.
 */
public class DeleteMechanismActivity extends BaseMechanismActivity {

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = LoggerFactory.getLogger(DeleteMechanismActivity.class);

        setContentView(R.layout.delete);

        final Mechanism mechanism = getMechanism();
        if (mechanism == null) {
            logger.error("Failed to find Mechanism to delete.");
            finish();
            return;
        }
        final Identity owner = mechanism.getOwner();
        ((TextView) findViewById(R.id.issuer)).setText(mechanism.getOwner().getIssuer());
        ((TextView) findViewById(R.id.label)).setText(mechanism.getOwner().getAccountName());
        Picasso.with(this)
                .load(mechanism.getOwner().getImage())
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
                owner.removeMechanism(mechanism);
                finish();
            }
        });
    }
}
