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
 *
 * Portions Copyright 2014 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.mechanisms.oath;

/**
 * Represents a currently active token.
 */
class TokenCode {
    private final String code;
    private final long start;
    private final long until;

    public TokenCode(String code, long start, long until) {
        this.code = code.substring(0, code.length() / 2) + " " +
                "" + code.substring(code.length() / 2, code.length());
        this.start = start;
        this.until = until;
    }

    /**
     * Gets the code which is currently active.
     * @return The currently active token.
     */
    public String getCurrentCode() {
        return code;
    }

    public boolean isValid() {
        long cur = System.currentTimeMillis();

        return cur < until;
    }


    public int getCurrentProgress() {
        long cur = System.currentTimeMillis();

        long total = until - start;
        long state = cur - start;
        return (int) (state * 1000 / total);
    }

}
