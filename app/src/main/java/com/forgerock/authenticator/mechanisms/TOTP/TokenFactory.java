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
 * Copyright 2015-2016 ForgeRock AS.
 */

package com.forgerock.authenticator.mechanisms.TOTP;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismFactory;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;

import java.util.Map;

/**
 * Responsible for generating instances of {@link Token}.
 *
 * Understands the concept of a version number associated with a Token
 * and will parse the URI according to this.
 */
class TokenFactory implements MechanismFactory {
    private final OTPAuthMapper mapper = new OTPAuthMapper();

    @Override
    public Mechanism createFromUri(String uri) throws MechanismCreationException, URIMappingException {

        Map<String, String> values = mapper.map(uri);
        int version = Integer.parseInt(get(values, OTPAuthMapper.VERSION, "1"));
        if (version == 1) {

            Identity identity = Identity.builder()
                    .setIssuer(get(values, OTPAuthMapper.ISSUER, ""))
                    .setLabel(get(values, OTPAuthMapper.LABEL, ""))
                    .setImage(get(values, "image", null))
                    .build();

            Token token = Token.getBuilder()
                    .setOwner(identity)
                    .setAlgorithm(get(values, OTPAuthMapper.ALGORITHM, "sha1"))
                    .setType(values.get(OTPAuthMapper.TYPE))
                    .setCounter(get(values, OTPAuthMapper.COUNTER, "0"))
                    .setDigits(get(values, OTPAuthMapper.DIGITS, "6"))
                    .setPeriod(get(values, OTPAuthMapper.PERIOD, "30"))
                    .setSecret(get(values, OTPAuthMapper.SECRET, ""))
                    .build();
            return token;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    @Override
    public Mechanism createFromParameters(int version, Identity owner, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            return Token.getBuilder()
                    .setOwner(owner)
                    .setOptions(map)
                    .build();

        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    private static String get(Map<String, String> map, String name, String defaultValue) {
        String value = map.get(name);
        return value == null ? defaultValue : value;
    }
}
