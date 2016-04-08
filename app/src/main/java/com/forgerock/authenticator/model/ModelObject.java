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

import android.content.Context;

import com.forgerock.authenticator.storage.IdentityDatabase;

import java.util.ArrayList;

/**
 * Base class for objects which are a part of the Identity Model.
 */
public abstract class ModelObject<T> implements Comparable<T> {
    /**
     * Default value for ids, indicating that the object has not been stored.
     */
    protected static final long NOT_STORED = -1;

    /**
     * Determines if the object has been stored.
     * @return True if the object has been stored, false otherwise.
     */
    public abstract boolean isStored();

    /**
     * Adds the object to the database if it has not been stored, otherwise updates it.
     * Should not be called from outside the object model.
     * @param context The context that the object is being saved from.
     */
    public abstract void save(Context context);

    /**
     * Deletes the object from the database. Should not be called from outside the object model.
     * @param context The context that the object is being deleted from.
     */
    public abstract void delete(Context context);

    /**
     * Gets an opaque reference to this object.
     * @return An opaque reference to this object.
     */
    public abstract ArrayList<String> getOpaqueReference();

    /**
     * Consumes the part of the reference that refers to this object, and returns true if such a
     * reference is found. Changes the reference that was passed in.
     * @param reference The opaque reference of an object.
     * @return True if the reference matches this object, false otherwise.
     */
    public abstract boolean consumeOpaqueReference(ArrayList<String> reference);

    /**
     * Called upon loading the model from the database. Should return true if the object and its
     * children are valid.
     * @return True if the model object is valid..
     */
    public abstract boolean validate();
}
