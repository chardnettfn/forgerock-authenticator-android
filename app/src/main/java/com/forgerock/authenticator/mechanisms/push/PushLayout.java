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

package com.forgerock.authenticator.mechanisms.push;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forgerock.authenticator.NotificationActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.base.MechanismLayout;
import com.forgerock.authenticator.message.MessageConstants;

/**
 * Handles the display of a Push mechanism in a list.
 */
public class PushLayout extends FrameLayout implements MechanismLayout<Push> {

    public PushLayout(Context context) {
        super(context);
    }

    public PushLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PushLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void bind(final Push mechanism) {
        TextView issuer = (TextView) findViewById(R.id.issuer);
        TextView label = (TextView) findViewById(R.id.label);

        issuer.setText(mechanism.getOwner().getIssuer());
        label.setText(mechanism.getOwner().getAccountName());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                Intent intent = new Intent(context, NotificationActivity.class);
                intent.putExtra(NotificationActivity.IDENTITY_REFERENCE, mechanism.getOwner().getOpaqueReference());
                context.startActivity(intent);
            }
        });
    }
}
