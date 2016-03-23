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

package com.forgerock.authenticator.mechanisms.oath;

import android.content.Context;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.base.MechanismFactory;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.Map;

import roboguice.RoboGuice;

/**
 * Responsible for generating instances of {@link Oath}.
 *
 * Understands the concept of a version number associated with a Token
 * and will parse the URI according to this.
 */
class OathFactory implements MechanismFactory {
    private final OathAuthMapper mapper = new OathAuthMapper();

    @Override
    public Mechanism createFromUri(Context context, String uri) throws MechanismCreationException, URIMappingException {

        Map<String, String> values = mapper.map(uri);
        int version = Integer.parseInt(get(values, OathAuthMapper.VERSION, "1"));
        if (version == 1) {

            String issuer = get(values, OathAuthMapper.ISSUER, "");
            String accountName = get(values, OathAuthMapper.ACCOUNT_NAME, "");
            String image = get(values, "image", null);

            IdentityModel identityModel = RoboGuice.getInjector(context).getInstance(IdentityModel.class);

            Identity identity = identityModel.getIdentity(issuer, accountName);

            if (identity == null) {
                identity = Identity.builder()
                        .setIssuer(issuer)
                        .setAccountName(accountName)
                        .setImage(image)
                        .build();
                identityModel.addIdentity(context, identity);
            }

            Oath.OathBuilder oathBuilder = Oath.getBuilder()
                    .setAlgorithm(get(values, OathAuthMapper.ALGORITHM, "sha1"))
                    .setType(values.get(OathAuthMapper.TYPE))
                    .setCounter(get(values, OathAuthMapper.COUNTER, "0"))
                    .setDigits(get(values, OathAuthMapper.DIGITS, "6"))
                    .setPeriod(get(values, OathAuthMapper.PERIOD, "30"))
                    .setSecret(get(values, OathAuthMapper.SECRET, ""));

            Mechanism oathAuth = identity.addMechanism(context, oathBuilder);
            return oathAuth;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    @Override
    public Mechanism.PartialMechanismBuilder restoreFromParameters(int version, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            return Oath.getBuilder()
                    .setOptions(map);

        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    private static String get(Map<String, String> map, String name, String defaultValue) {
        String value = map.get(name);
        return value == null ? defaultValue : value;
    }
}
