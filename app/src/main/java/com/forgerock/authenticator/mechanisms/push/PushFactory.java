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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.MechanismFactory;
import com.forgerock.authenticator.mechanisms.URIMappingException;

import java.util.Map;

/**
 * Responsible for generating instances of {@link Push}.
 *
 * Understands the concept of a version number associated with a Push mechanism
 * and will parse the URI according to this.
 */
public class PushFactory implements MechanismFactory {
    private PushAuthMapper mapper = new PushAuthMapper();

    @Override
    public Mechanism createFromUri(String uri) throws URIMappingException, MechanismCreationException {
        Map<String, String> values = mapper.map(uri);
        int version;
        try {
            version = Integer.parseInt(get(values, PushAuthMapper.VERSION, "1"));
        } catch (NumberFormatException e) {
            throw new MechanismCreationException("Expected valid integer, found " +
                    get(values, PushAuthMapper.VERSION, "1"), e);
        }
        if (version == 1) {
            Identity identity = Identity.builder()
                    .setIssuer(get(values, PushAuthMapper.ISSUER, ""))
                    .setAccountName(get(values, PushAuthMapper.ACCOUNT_NAME, ""))
                    .setImage(get(values, "image", null))
                    .build();

            Push pushAuth = Push.builder()
                    .setOwner(identity)
                    .build();
            return pushAuth;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }

    }
    @Override
    public Mechanism createFromParameters(int version, Identity owner, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            return Push.builder()
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
