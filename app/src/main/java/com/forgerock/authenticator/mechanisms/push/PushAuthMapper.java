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

    /**
     * Keys.
     */

    /** The endpoint used for registration */
    public static final String REG_ENDPOINT = "r";
    /** The endpoint used for authentication */
    public static final String AUTH_ENDPOINT = "a";
    /** The message id to use for response */
    public static final String MESSAGE_ID = "m";
    /** The shared secret used for signing */
    public static final String SHARED_SECRET = "s";
    /** The background color used for theming */
    public static final String BACKGROUND_COLOR = "b"; // TODO: Move this to shared code
    /** The challenge to use for the response */
    public static final String CHALLENGE = "c";

    @Override
    protected Map<String, String> validate(Map<String, String> values) throws URIMappingException {

        //TODO: validate appropriately (in parallel with unit tests)
        values.put(IMAGE, new String(Base64.decode(values.get(IMAGE), Base64.URL_SAFE))); // Have to decode the url here - how does auth work?

        if (!values.containsKey(MESSAGE_ID) || values.get(MESSAGE_ID).isEmpty()) {
            throw new URIMappingException("Message ID is required");
        }

        return values;
    }
}
