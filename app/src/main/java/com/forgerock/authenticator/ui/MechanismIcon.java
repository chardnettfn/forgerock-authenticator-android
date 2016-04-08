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

package com.forgerock.authenticator.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.base.Mechanism;

/**
 * UI element which is an icon representation of a Mechanism. Contains both the icon for the
 * Mechanism, as well as a badge which displays the number of notifications attached to that
 * Mechanism.
 */
public class MechanismIcon extends FrameLayout {

    /**
     * Create an icon that a will represent a Mechanism.
     * @param context The context that the view is created from.
     */
    public MechanismIcon(Context context) {
        super(context);
        init();
    }

    /**
     * Create an icon that a will represent a Mechanism.
     * @param context The context that the view is created from.
     * @param attrs The list of attributes.
     */
    public MechanismIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Create an icon that a will represent a Mechanism.
     * @param context The context that the view is created from.
     * @param attrs The list of attributes.
     * @param defStyleAttr The resource containing default style attributes.
     */
    public MechanismIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.mechanismicon, this);
    }

    /**
     * Set the Mechanism that this icon represents.
     * @param mechanism The Mechanism that is represented by this icon.
     */
    public void setMechanism(Mechanism mechanism) {
        ImageView icon = (ImageView) findViewById(R.id.icon_image);
        icon.setImageDrawable(getResources().getDrawable(mechanism.getInfo().getIcon()));
        setNotificationNumber(mechanism.getNotifications().size());
    }

    private void setNotificationNumber(int notificationNumber) {
        TextView badge = ((TextView) findViewById(R.id.badge));
        if (notificationNumber == 0) {
            badge.setVisibility(GONE);
        } else {
            badge.setVisibility(VISIBLE);
            badge.setText(Integer.toString(notificationNumber));
        }
    }
}
