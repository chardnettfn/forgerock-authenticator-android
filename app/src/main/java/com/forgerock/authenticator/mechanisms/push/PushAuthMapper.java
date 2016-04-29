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

import android.util.Base64;

import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.mechanisms.base.UriParser;

import java.util.Map;

/**
 * Provides the ability to parse URI scheme into a convenient format
 * to use with configuring a {@link Push} to receive push notifications.
 */
public class PushAuthMapper extends UriParser {

    /** The secret used for generating the OTP */
    public static final String MESSAGE_ID = "m";
    public static final String REG_ENDPOINT = "r";
    public static final String AUTH_ENDPOINT = "a";

    @Override
    protected Map<String, String> validate(Map<String, String> values) throws URIMappingException {

        values.put(REG_ENDPOINT, new String(Base64.decode(values.get(REG_ENDPOINT), Base64.URL_SAFE)));
        values.put(AUTH_ENDPOINT, new String(Base64.decode(values.get(AUTH_ENDPOINT), Base64.URL_SAFE)));

        if (!values.containsKey(MESSAGE_ID) || values.get(MESSAGE_ID).isEmpty()) {
            throw new URIMappingException("Message ID is required");
        }

        return values;
    }
}
