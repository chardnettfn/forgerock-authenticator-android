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

import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.mechanisms.base.UriParser;

import org.forgerock.util.encode.Base64;
import org.forgerock.util.encode.Base64url;

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
    public static final String REG_ENDPOINT_KEY = "r";
    /** The endpoint used for authentication */
    public static final String AUTH_ENDPOINT_KEY = "a";
    /** The message id to use for response */
    public static final String MESSAGE_ID_KEY = "m";
    /** The shared secret used for signing */
    public static final String BASE_64_SHARED_SECRET_KEY = "s";
    /** The challenge to use for the response */
    public static final String BASE_64_CHALLENGE_KEY = "c";
    /** The challenge to use for the response */
    public static final String AM_LOAD_BALANCER_COOKIE_KEY = "l";

    private static final String BASE_64_URL_SHARED_SECRET = "s";
    private static final String BASE_64_URL_CHALLENGE = "c";
    private static final String BASE_64_URL_IMAGE = "image";
    private static final String BASE_64_URL_REG_ENDPOINT = "r";
    private static final String BASE_64_URL_AUTH_ENDPOINT = "a";
    private static final String BASE_64_AM_LOAD_BALANCER_COOKIE_KEY = "l";
    
    @Override
    protected Map<String, String> postProcess(Map<String, String> values) throws URIMappingException {

        if (!containsNonEmpty(values, MESSAGE_ID_KEY)) {
            throw new URIMappingException("Message ID is required");
        }

        if (containsNonEmpty(values, BASE_64_URL_IMAGE)) {
            values.put(IMAGE, new String(Base64url.decode(values.get(BASE_64_URL_IMAGE))));
        }

        values.put(REG_ENDPOINT_KEY, recodeBase64UrlValueToStringWithValidation(values, BASE_64_URL_REG_ENDPOINT));
        values.put(AM_LOAD_BALANCER_COOKIE_KEY, recodeBase64UrlValueToStringWithValidation(values, BASE_64_AM_LOAD_BALANCER_COOKIE_KEY));
        values.put(ISSUER_KEY, recodeBase64UrlValueToStringWithValidation(values, ISSUER_KEY));
        values.put(AUTH_ENDPOINT_KEY, recodeBase64UrlValueToStringWithValidation(values, BASE_64_URL_AUTH_ENDPOINT));
        values.put(BASE_64_SHARED_SECRET_KEY, recodeBase64UrlValueToBase64WithValidation(values, BASE_64_URL_SHARED_SECRET));
        values.put(BASE_64_CHALLENGE_KEY, recodeBase64UrlValueToBase64WithValidation(values, BASE_64_URL_CHALLENGE));

        return values;
    }

    byte[] decodeValueWithValidation(Map<String, String> data, String key) throws URIMappingException{
        if (!containsNonEmpty(data, key)) {
            throw new URIMappingException(key + " must not be empty");
        }
        byte[] bytes = Base64url.decode(data.get(key));

        if (bytes == null) {
            throw new URIMappingException("Failed to decode value in " + key);
        }
        return bytes;
    }

    String recodeBase64UrlValueToBase64WithValidation(Map<String, String> data, String key) throws URIMappingException{
        return Base64.encode(decodeValueWithValidation(data, key));
    }

    String recodeBase64UrlValueToStringWithValidation(Map<String, String> data, String key) throws URIMappingException{
        return new String(decodeValueWithValidation(data, key));
    }
}
