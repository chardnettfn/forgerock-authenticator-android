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

package com.forgerock.authenticator.mechanisms.TOTP;

/**
 * Represents a currently active token.
 */
class TokenCode {
    private final String code;
    private final long start;
    private final long until;
    private TokenCode next;

    public TokenCode(String code, long start, long until) {
        this.code = code;
        this.start = start;
        this.until = until;
    }

    public TokenCode(String code, long start, long until, TokenCode next) {
        this(code, start, until);
        this.next = next;
    }

    /**
     * Gets the code which is currently active.
     * @return The currently active token.
     */
    public String getCurrentCode() {
        TokenCode active = getActive(System.currentTimeMillis());
        if (active == null)
            return null;
        return active.code;
    }

    public int getTotalProgress() {
        long cur = System.currentTimeMillis();
        long total = getLast().until - start;
        long state = total - (cur - start);
        return (int) (state * 1000 / total);
    }

    public int getCurrentProgress() {
        long cur = System.currentTimeMillis();
        TokenCode active = getActive(cur);
        if (active == null)
            return 0;

        long total = active.until - active.start;
        long state = total - (cur - active.start);
        return (int) (state * 1000 / total);
    }

    private TokenCode getActive(long curTime) {
        if (curTime >= start && curTime < until)
            return this;

        if (next == null)
            return null;

        return this.next.getActive(curTime);
    }

    private TokenCode getLast() {
        if (next == null)
            return this;
        return this.next.getLast();
    }
}
