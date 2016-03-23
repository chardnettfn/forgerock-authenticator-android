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

package com.forgerock.authenticator.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;

/**
 * Individual entry which displays information about a given Notification.
 */
public class NotificationLayout extends FrameLayout {
    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     */
    public NotificationLayout(Context context) {
        super(context);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     * @param defStyleAttr The resource containing default style attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set the Notification that this Layout displays.
     * @param notification The Notification to display.
     */
    void bind(final Notification notification) {
        TextView issuerView = (TextView) findViewById(R.id.issuer);
        TextView messageView = (TextView) findViewById(R.id.message);
        TextView timeView = (TextView) findViewById(R.id.time);
        issuerView.setText(notification.getMechanism().getOwner().getIssuer());
        messageView.setText("Login requested");
        timeView.setText("10 Minutes ago");
        final Context context = getContext();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PushAuthActivity.class);
                intent.putExtra(PushAuthActivity.NOTIFICATION_REFERENCE, notification.getOpaqueReference());
                context.startActivity(intent);
            }
        });
    }
}
