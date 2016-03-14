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

package com.forgerock.authenticator.mechanisms.TOTP;

import com.forgerock.authenticator.utils.OTPAuthMapper;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.Map;

/**
 * Responsible for generating instances of {@link Token}.
 *
 * Understands the concept of a version number associated with a Token
 * and will parse the URI according to this.
 */
public class TokenFactory {
    private final OTPAuthMapper mapper = new OTPAuthMapper();

    /**
     * @param uri Non null configuration URI.
     * @return An instance of a Token which represents the configuration URI.
     * @throws URIMappingException If there was any unrecoverable parsing problem.
     */
    public Token get(String uri) throws URIMappingException {
        Token token = new Token();

        Map<String, String> values = mapper.map(uri);
        int version = Integer.parseInt(get(values, OTPAuthMapper.VERSION, "1"));
        if (version == 1) {
            token.setType(values.get(OTPAuthMapper.TYPE));
            token.setIssuer(get(values, OTPAuthMapper.ISSUER, ""));
            token.setLabel(get(values, OTPAuthMapper.LABEL, ""));
            token.setAlgorithm(get(values, OTPAuthMapper.ALGORITHM, "sha1"));
            token.setDigits(get(values, OTPAuthMapper.DIGITS, "6"));
            token.setPeriod(get(values, OTPAuthMapper.PERIOD, "30"));
            token.setSecret(get(values, OTPAuthMapper.SECRET, ""));
            if (token.getType() == Token.TokenType.HOTP) {
                token.setCounter(get(values, OTPAuthMapper.COUNTER, "0"));
            }
            token.setImage(get(values, "image", null));
        } else {
            throw new URIMappingException("Unknown version: " + version);
        }

        return token;
    }

    private static String get(Map<String, String> m, String name, String def) {
        String r = m.get(name);
        return r == null ? def : r;
    }
}
