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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.mechanisms.base.Mechanism.PartialMechanismBuilder;
import com.forgerock.authenticator.mechanisms.push.PushAuthMapper;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.Map;

import roboguice.RoboGuice;

/**
 * Mechanism specific factory which can convert the two storage methods to a Mechanism.
 */
public abstract class MechanismFactory {

    /**
     * Convert a URL to the Mechanism it represents, including extracting the owner.
     * Also adds it to the model.
     * @param uri The URI to process.
     * @return The created Mechanism.
     * @throws URIMappingException If the URL was not parsed correctly.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public final Mechanism createFromUri(Context context, String uri) throws URIMappingException, MechanismCreationException {
        Map<String, String> values = getParser().map(uri);
        String issuer = get(values, UriParser.ISSUER, "");
        String accountName = get(values, UriParser.ACCOUNT_NAME, "");
        String image = get(values, UriParser.IMAGE, null);
        int version;
        try {
            version = Integer.parseInt(get(values, UriParser.VERSION, "1"));
        } catch (NumberFormatException e) {
            throw new MechanismCreationException("Expected valid integer, found " +
                    get(values, PushAuthMapper.VERSION, "1"), e);
        }

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

        int mechanismUID = identityModel.getNewMechanismUID();

        PartialMechanismBuilder builder = createFromUriParameters(context, version, mechanismUID, values)
                .setMechanismUID(mechanismUID);
        Mechanism mechanism = identity.addMechanism(context, builder);
        return mechanism;
    }

    /**
     * Internal method used to create the incomplete builder for the Mechanism represented by a
     * given URI.
     * @param version The version extracted from the URI.
     * @param mechanismUID A generated mechanismUID, used to inform this function of the value.
     *                     Does not need to be used, as it is added by the calling function.
     * @param map The map of values generated from the original URI.
     * @return The incomplete MechanismBuilder.
     * @throws MechanismCreationException If anything goes wrong.
     */
    protected abstract PartialMechanismBuilder createFromUriParameters(
            Context context, int version, int mechanismUID, Map<String, String> map)
            throws MechanismCreationException;

    /**
     * Return the UriParser subclass used by the factory for a particular Mechanism type.
     * @return The UriParser.
     */
    protected abstract UriParser getParser();

    /**
     * Uses the map to create the partial mechanism builder associated with the version provided.
     * This is used to restore a mechanism that has been stored.
     * The resulting Mechanism builder must be added to the owner.
     * @param version The version of the Mechanism being created.
     * @param map The set of properties.
     * @return The create Mechanism.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public abstract PartialMechanismBuilder restoreFromParameters(int version, Map<String, String> map)
            throws MechanismCreationException;

    protected final String get(Map<String, String> map, String name, String defaultValue) {
        String value = map.get(name);
        return value == null ? defaultValue : value;
    }
}
