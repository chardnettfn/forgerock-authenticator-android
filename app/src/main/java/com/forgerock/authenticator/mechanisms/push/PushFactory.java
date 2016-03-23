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

import android.content.Context;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.MechanismFactory;
import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.Map;

import roboguice.RoboGuice;

/**
 * Responsible for generating instances of {@link Push}.
 *
 * Understands the concept of a version number associated with a Push mechanism
 * and will parse the URI according to this.
 */
public class PushFactory implements MechanismFactory {
    private PushAuthMapper mapper = new PushAuthMapper();

    @Override
    public Mechanism createFromUri(Context context, String uri) throws URIMappingException, MechanismCreationException {
        Map<String, String> values = mapper.map(uri);
        int version;
        try {
            version = Integer.parseInt(get(values, PushAuthMapper.VERSION, "1"));
        } catch (NumberFormatException e) {
            throw new MechanismCreationException("Expected valid integer, found " +
                    get(values, PushAuthMapper.VERSION, "1"), e);
        }
        if (version == 1) {
            IdentityModel identityModel = RoboGuice.getInjector(context).getInstance(IdentityModel.class);

            String issuer = get(values, PushAuthMapper.ISSUER, "");
            String accountName = get(values, PushAuthMapper.ACCOUNT_NAME, "");
            String image = get(values, "image", null);

            Identity identity = identityModel.getIdentity(issuer, accountName);

            if (identity == null) {
                identity = Identity.builder()
                        .setIssuer(issuer)
                        .setAccountName(accountName)
                        .setImage(image)
                        .build();
                identityModel.addIdentity(context, identity);
            }

            Push.PushBuilder pushBuilder = Push.builder()
                    .setMechanismUID(5);
            //TODO: Get real authentication UID

            Mechanism pushAuth = identity.addMechanism(context, pushBuilder);

            return pushAuth;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }

    }
    @Override
    public Mechanism.PartialMechanismBuilder restoreFromParameters(int version, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            return Push.builder()
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
