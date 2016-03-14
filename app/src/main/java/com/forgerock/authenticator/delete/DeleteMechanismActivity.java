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
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Activity responsible for verifying that a user wishes to delete a mechanism, then deleting it.
 * The rowId of the mechanism to delete must be passed in using the Intent.
 */
public class DeleteMechanismActivity extends RoboActivity {

    /** Used to pass in the RowId of the Mechanism to delete */
    public static final String ROW_ID = "rowid";

    private Logger logger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = LoggerFactory.getLogger(DeleteMechanismActivity.class);


        final long rowId = getIntent().getLongExtra(ROW_ID, -1);
        assert rowId >= 0;

        setContentView(R.layout.delete);
        Mechanism token;
        try {
            token = getIdentityDatabase().getMechanism(rowId);
        } catch (MechanismCreationException e) {
            logger.error("Failed to find Mechainsm to delete", e);
            finish();
            return;
        }
        ((TextView) findViewById(R.id.issuer)).setText(token.getOwner().getIssuer());
        ((TextView) findViewById(R.id.label)).setText(token.getOwner().getLabel());
        Picasso.with(this)
                .load(token.getOwner().getImage())
                .placeholder(R.drawable.forgerock_logo)
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
                getIdentityDatabase().deleteMechanism(rowId);
                finish();
            }
        });
    }

    /**
     * Gets the database used for storing the data. Exposed for testing.
     * @return The shared database connection.
     */
    public IdentityDatabase getIdentityDatabase() {
        return RoboGuice.getInjector(this).getInstance(IdentityDatabase.class);
    }
}
