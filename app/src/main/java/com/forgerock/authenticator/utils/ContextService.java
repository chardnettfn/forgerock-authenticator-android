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
package com.forgerock.authenticator.utils;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * Responsible for resolving context based Services which are required for performing
 * actions within the Android framework.
 *
 * Wraps around the complexity and provides validation where appropriate.
 */
public class ContextService {
    /**
     * Acquire an instance of a Service as defined by the name.
     *
     * @param context Non null, required for Android operations.
     * @param serviceName Non null, must be one of the services defined by the {@link Context} class
     *                    e.g. {@link Context#NOTIFICATION_SERVICE}.
     * @param <T> The expected return type, provided as a convenience as this function will
     *           perform the cast on the behalf of the caller.
     * @return Non null instance of the requested service.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Context context, String serviceName) {

        boolean match = false;
        for (Field field : Context.class.getDeclaredFields()) {
            String name = field.getName();
            if (name.endsWith("SERVICE") && serviceName.equals(name)) {
                match = true;
                break;
            }
        }
        if (!match) {
            throw new IllegalArgumentException("Invalid Service requested " + serviceName);
        }

        try {
            /**
             * Later Android SDK provides a getSystemService(Class) method, but as we target SDK 14 we depend on
             * the previous call.
             */
            return (T) context.getSystemService(serviceName);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Requested a Service which could not be cast to the return type.");
        }
    }
}
