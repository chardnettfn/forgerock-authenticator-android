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
 * Copyright 2015 ForgeRock AS.
 */
package com.forgerock.authenticator.utils;

import com.forgerock.authenticator.mechanisms.TOTP.Token;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the ability to parse URI scheme into a convenient format
 * to use with with configuring a {@link Token}
 * to generate OTP codes.
 *
 * The configuration URI is based on the format defined by the original
 * Google Authenticator project:
 *
 * https://github.com/google/google-authenticator/wiki/Key-Uri-Format
 */
public class OTPAuthMapper {
    public static final String SCHEME = "scheme";

    public static final String TYPE = "authority"; // URI refers to this as the authority.
    public static final String[] ALLOWED_TYPES = new String[]{"hotp", "totp"};

    public static final String ISSUER = "issuer";
    public static final String LABEL = "accountname";

    public static final String SECRET = "secret";
    public static final String ALGORITHM = "algorithm";
    public static final String DIGITS = "digits";
    public static final String COUNTER = "counter";
    public static final String PERIOD = "period";

    public static final String SLASH = "/";

    // URI API Version
    public static final String VERSION = "version";

    /**
     * Call through to {@link OTPAuthMapper#map(URI)}
     *
     * @param uriScheme Non null.
     * @return Non null, possibly empty Map.
     * @throws URIMappingException If there was an unexpected error parsing.
     */
    public Map<String, String> map(String uriScheme) throws URIMappingException {
        try {
            return validate(map(new URI(uriScheme)));
        } catch (URISyntaxException e) {
            throw new URIMappingException("Failed to parse URI", e);
        }
    }

    /**
     * Parse the URI into a more useful Map format with known keys.
     *
     * Makes use of the Java provided {@link URI} to simplify parsing.
     *
     * @param uri Non null URI to parse.
     * @return Non null possibly empty Map.
     * @throws URIMappingException If there was an unexpected error parsing.
     */
    private Map<String, String> map(URI uri) throws URIMappingException {
        Map<String, String> r = new HashMap<String, String>();
        r.put(SCHEME, uri.getScheme());
        r.put(TYPE, uri.getAuthority());

        // Label may contain Issuer and Account Name
        String path = stripSlash(uri.getPath());
        String[] pathParts = split(path, ":");
        if (pathParts == null) {
            r.put(LABEL, path);
        } else {
            r.put(ISSUER, pathParts[0]);
            r.put(LABEL, pathParts[1]);
        }

        Collection<String> queryParts = Collections.emptySet();
        if (uri.getQuery() != null) {
            queryParts = Arrays.asList(uri.getQuery().split("&"));
        }
        for (String query : queryParts) {
            String[] split = split(query, "=");
            if (split != null) {
                r.put(split[0], split[1]);
            }
        }
        return r;
    }

    /**
     * Validates the parsed URI values based on the requirements of the current
     * Google Authenticator specification.
     *
     * @param values Non null.
     * @return A non null Map.
     * @throws URIMappingException If there were any validation errors.
     */
    private Map<String, String> validate(Map<String, String> values) throws URIMappingException {
        // Validate Type
        String type = values.get(TYPE);
        boolean validType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equalsIgnoreCase(type)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw new URIMappingException(MessageFormat.format("Type {0} was not valid", type));
        }

        // Secret is REQUIRED
        if (!values.containsKey(SECRET)) {
            throw new URIMappingException("Secret is required");
        }

        // Counter is REQUIRED
        if (type.equalsIgnoreCase(ALLOWED_TYPES[0])) {
            if (!values.containsKey(COUNTER)) {
                throw new URIMappingException("Counter is required when in hotp mode");
            }
        }

        return values;
    }

    private static String[] split(String s, String sep) {
        int index = s.indexOf(sep);
        if (index == -1) {
            return null;
        }
        return new String[]{
                s.substring(0, index),
                s.substring(index + sep.length(), s.length())};
    }

    private static String stripSlash(String s) {
        if (s.startsWith(SLASH)) {
            return stripSlash(s.substring(SLASH.length(), s.length()));
        }
        if (s.endsWith(SLASH)) {
            return stripSlash(s.substring(0, s.length() - SLASH.length()));
        }
        return s;
    }
}
