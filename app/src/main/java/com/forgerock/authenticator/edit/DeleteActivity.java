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

package com.forgerock.authenticator.edit;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.Mechanism;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.utils.MechanismCreationException;
import com.squareup.picasso.Picasso;

public class DeleteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete);
        Mechanism token;
        try {
            token = getIdentityDatabase().getMechanism(getRowId());
        } catch (MechanismCreationException e) {
            e.printStackTrace();
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
                getIdentityDatabase().deleteMechanism(getRowId());
                finish();
            }
        });
    }
}
