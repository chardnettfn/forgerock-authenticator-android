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

package com.forgerock.authenticator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * List of ModelObjects which reorders itself whenever an element is added to the list.
 * @param <T> The particular class of ModelObject that is being stored.
 */
public class SortedList<T extends ModelObject> extends ArrayList<T> {

    @Override
    public boolean add(T object) {
        boolean result = super.add(object);
        Collections.sort(this);
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean result = super.addAll(collection);
        Collections.sort(this);
        return result;
    }
}
