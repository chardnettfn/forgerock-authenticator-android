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

import android.view.View;

/**
 * Provides information about a type of mechanism.
 * Collects together all the information required to create, store and wire the mechanism into the
 * user interface.
 */
public interface MechanismInfo {
    /**
     * Bind the mechanism to the given view (assumes that they are correctly matched).
     * @param mechanism The mechanism to bind.
     * @param view The view to bind it to.
     */
    void bind(View view, Mechanism mechanism);

    /**
     * Get the layout id for this layout type.
     * @return The layout id.
     */
    int getLayoutType();

    /**
     * Get the string used to represent this type in the database.
     * @return The mechanism string.
     */
    String getMechanismString();

    /**
     * Get the factory appropriate for this mechanism.
     * @return The mechanism factory.
     */
    MechanismFactory getFactory();

    /**
     * Determines if the provided URI is for this type of mechanism.
     * @param uri The URI to check.
     * @return True if the URI matches this type of mechanism, false otherwise.
     */
    boolean matchesURI(String uri);
}
