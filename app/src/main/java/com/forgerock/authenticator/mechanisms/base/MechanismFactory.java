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

package com.forgerock.authenticator.mechanisms.base;

import android.content.Context;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;

import java.util.Map;

/**
 * Mechanism specific factory which can convert the two storage methods to a Mechanism.
 */
public interface MechanismFactory {

    /**
     * Convert a URL to the Mechanism it represents, including extracting the owner. Also saves it to the database.
     * @param uri The URI to process.
     * @return The created Mechanism.
     * @throws URIMappingException If the URL was not parsed correctly.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    Mechanism createFromUri(Context context, String uri) throws URIMappingException, MechanismCreationException;


    /**
     * Uses the map to create the partial mechanism builder associated with the version provided.
     * This is used to restore a mechanism that has been stored.
     * The resulting Mechanism builder must be added to the owner.
     * @param version The version of the Mechanism being created.
     * @param map The set of properties.
     * @return The create Mechanism.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    Mechanism.PartialMechanismBuilder restoreFromParameters(int version, Map<String, String> map) throws MechanismCreationException;
}
