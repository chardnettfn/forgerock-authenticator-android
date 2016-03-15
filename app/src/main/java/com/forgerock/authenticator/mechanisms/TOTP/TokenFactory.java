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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.IMechanismFactory;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.forgerock.authenticator.utils.OTPAuthMapper;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Responsible for generating instances of {@link Token}.
 *
 * Understands the concept of a version number associated with a Token
 * and will parse the URI according to this.
 */
public class TokenFactory implements IMechanismFactory {
    private final OTPAuthMapper mapper = new OTPAuthMapper();

    @Override
    public String getMechanismString() {
        return "OTP";
    }

    /**
     * @param uri Non null configuration URI.
     * @return An instance of a Token which represents the configuration URI.
     * @throws URIMappingException If there was any unrecoverable parsing problem.
     */
    public Mechanism get(String uri) throws MechanismCreationException, URIMappingException {

        Map<String, String> values = mapper.map(uri);
        int version = Integer.parseInt(get(values, OTPAuthMapper.VERSION, "1"));
        if (version == 1) {
            Identity identity = new Identity();
            identity.setIssuer(get(values, OTPAuthMapper.ISSUER, ""));
            identity.setLabel(get(values, OTPAuthMapper.LABEL, ""));
            identity.setImage(get(values, "image", null));

            Token token = new Token(identity);

            token.setType(values.get(OTPAuthMapper.TYPE));

            token.setAlgorithm(get(values, OTPAuthMapper.ALGORITHM, "sha1"));
            token.setDigits(get(values, OTPAuthMapper.DIGITS, "6"));
            token.setPeriod(get(values, OTPAuthMapper.PERIOD, "30"));
            token.setSecret(get(values, OTPAuthMapper.SECRET, ""));
            if (token.getType() == Token.TokenType.HOTP) {
                token.setCounter(get(values, OTPAuthMapper.COUNTER, "0"));
            }
            return token;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    @Override
    public Mechanism get(int version, Identity owner, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            return new Token(owner, map);
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    private static String get(Map<String, String> m, String name, String def) {
        String r = m.get(name);
        return r == null ? def : r;
    }
}
