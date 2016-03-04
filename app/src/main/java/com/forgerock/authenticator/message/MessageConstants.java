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

package com.forgerock.authenticator.message;

/**
 * Constants for Messages
 */
public final class MessageConstants {

    /** Private constructor */
    private MessageConstants() { }

    /** A keyword to indicate that the token has been sent to the server. */
    public static final String TOKEN_SENT_TO_SERVER = "tokenSentToServer";
    /** A keyword to indicate that the token has been received by the server. */
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    /**
     * Signalling fields for a downstream message to display on a UI Activity
     * for the user.
     */
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
}
