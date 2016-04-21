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
import android.support.annotation.VisibleForTesting;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.base.MechanismFactory;
import com.forgerock.authenticator.mechanisms.base.UriParser;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.Map;

/**
 * Responsible for generating instances of {@link Oath}.
 *
 * Understands the concept of a version number associated with a Token
 * and will parse the URI according to this.
 */
class OathFactory extends MechanismFactory {
    private final OathAuthMapper mapper = new OathAuthMapper();

    protected OathFactory(Context context, IdentityModel model) {
        super(context, model);
    }

    @Override
    protected Mechanism.PartialMechanismBuilder createFromUriParameters(
        int version, int mechanismUID, Map<String, String> map) throws MechanismCreationException {
        if (version == 1) {
            Oath.OathBuilder oathBuilder = Oath.getBuilder()
                    .setAlgorithm(get(map, OathAuthMapper.ALGORITHM, "sha1"))
                    .setType(map.get(OathAuthMapper.TYPE))
                    .setCounter(get(map, OathAuthMapper.COUNTER, "0"))
                    .setDigits(get(map, OathAuthMapper.DIGITS, "6"))
                    .setPeriod(get(map, OathAuthMapper.PERIOD, "30"))
                    .setSecret(get(map, OathAuthMapper.SECRET, ""));
            return oathBuilder;
        } else {
            throw new MechanismCreationException("Unknown version: " + version);
        }
    }

    @Override
    protected UriParser getParser() {
        return mapper;
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
}
