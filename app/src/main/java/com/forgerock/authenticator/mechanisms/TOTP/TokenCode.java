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

public class TokenCode {
    private final String mCode;
    private final long mStart;
    private final long mUntil;
    private TokenCode mNext;

    public TokenCode(String code, long start, long until) {
        mCode = code;
        mStart = start;
        mUntil = until;
    }

    public TokenCode(TokenCode prev, String code, long start, long until) {
        this(code, start, until);
        prev.mNext = this;
    }

    public TokenCode(String code, long start, long until, TokenCode next) {
        this(code, start, until);
        mNext = next;
    }

    public String getCurrentCode() {
        TokenCode active = getActive(System.currentTimeMillis());
        if (active == null)
            return null;
        return active.mCode;
    }

    public int getTotalProgress() {
        long cur = System.currentTimeMillis();
        long total = getLast().mUntil - mStart;
        long state = total - (cur - mStart);
        return (int) (state * 1000 / total);
    }

    public int getCurrentProgress() {
        long cur = System.currentTimeMillis();
        TokenCode active = getActive(cur);
        if (active == null)
            return 0;

        long total = active.mUntil - active.mStart;
        long state = total - (cur - active.mStart);
        return (int) (state * 1000 / total);
    }

    private TokenCode getActive(long curTime) {
        if (curTime >= mStart && curTime < mUntil)
            return this;

        if (mNext == null)
            return null;

        return this.mNext.getActive(curTime);
    }

    private TokenCode getLast() {
        if (mNext == null)
            return this;
        return this.mNext.getLast();
    }
}
