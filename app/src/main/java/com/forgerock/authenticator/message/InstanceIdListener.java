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

package com.forgerock.authenticator.message;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Respond to a request from Instance ID service to refresh the registration
 * token which may be stored on the device if registration has been performed.
 *
 * The registration token represents a combination of the user's device id, the
 * sender id of the service they are accessing and other details. It is also
 * the same mechanism by which a sender can directly address a user's devices
 * via the GCM network.
 *
 * Refresh can occur for a number of reasons:
 *
 * <li>Periodically (every 6 months)</li>
 * <li>If there are security issues (SSL failure)</li>
 * <li>Device information is no longer valid</li>
 *
 * {@see https://developers.google.com/instance-id/guides/android-implementation#refresh_tokens}
 */
public class InstanceIdListener extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {

        /* TODO: once work has been done to communicate with the server this will need to notify
         * the server of changes to the messaging token
         */

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, InstanceIdListener.class);
        startService(intent);
    }
}