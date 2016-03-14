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

package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;

import java.util.HashMap;
import java.util.Map;

/**
 * Determines the type of mechanism which is being created, and routes the creation request to the
 * appropriate builder.
 */
public class CoreMechanismFactory {
    private Map<String, MechanismFactory> factories;

    /**
     * Creates the CoreMechanismFactory and loads the available mechanism information.
     */
    public CoreMechanismFactory() {
        factories = new HashMap<>();
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            addFactory(info);
        }
    }

    private void addFactory(MechanismInfo info) {
        factories.put(info.getMechanismString(), info.getFactory());
    }

    /**
     * Convert a URL to the Mechanism it contains, including extracting the owner.
     * @param uri The URI to process.
     * @return The created Mechanism.
     * @throws URIMappingException If the URL was not parsed correctly.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public Mechanism createFromUri(String uri) throws URIMappingException, MechanismCreationException {
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            if (info.matchesURI(uri)) {
                return factories.get(info.getMechanismString()).createFromUri(uri);
            }
        }
        throw new MechanismCreationException("Unknown URI structure");
    }

    /**
     * Uses the map to create the Mechanism associated with the type and version provided, and
     * links it to the owner.
     * @param type The mechanism string for the Mechanism.
     * @param version The version of the Mechanism being created.
     * @param owner The owner of the mechanism.
     * @param map The set of properties.
     * @return The create Mechanism.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public Mechanism createFromParameters(String type, int version, Identity owner, Map<String, String> map) throws MechanismCreationException {
        if(!factories.containsKey(type)) {
            throw new MechanismCreationException("Unknown mechanism type stored \"" + type + "\"");
        }
        return factories.get(type).createFromParameters(version, owner, map);
    }
}
