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

package com.forgerock.authenticator.storage;

/**
 * Thrown if methods are called on an object that assumes it has been stored, when it has not.
 */
public class NotStoredException extends Exception {
    /**
     * Creates the exception with the provided message.
     * @param detailMessage A message describing the cause of the exception.
     */
    public NotStoredException(String detailMessage) {
        super(detailMessage);
    }
}
