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
import android.content.Intent;

/**
 * Responsible for generating instances of Intents to be used through the application.
 *
 * Identifies and simplifies construction of differing types of Intent.
 */
public class IntentFactory {
    /**
     * Generate an internal Intent which is used for signalling within the application.
     *
     * @param context Non null, required for Android operations
     * @param componentClass The class of the Intent to generate.
     * @return A non null Intent for this purpose.
     */
    public Intent generateInternal(Context context, Class<?> componentClass) {
        return new Intent(context, componentClass);
    }
}
