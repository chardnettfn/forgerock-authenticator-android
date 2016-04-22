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

import com.forgerock.authenticator.utils.TimeKeeper;

/**
 * Represents a currently active token.
 */
class TokenCode {
    private final String code;
    private final long start;
    private final long until;
    private TimeKeeper timeKeeper;
    private final int MAX_VALUE = 1000;

    public TokenCode(TimeKeeper timeKeeper, String code, long start, long until) {
        this.timeKeeper = timeKeeper;
        this.code = code;
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

    /**
     * Returns true if the TokenCode has not yet expired.
     * @return True if the TokenCode is still valid, false otherwise.
     */
    public boolean isValid() {
        long cur = timeKeeper.getCurrentTimeMillis();

        return cur < until;
    }

    /**
     * Get the current progress of the TokenCode. This is a number between 0 and 1000, and represents
     * the amount of time that has passed between the start and end times of the code.
     * @return The total progress, a number between 0 and 1000.
     */
    public int getCurrentProgress() {
        long cur = timeKeeper.getCurrentTimeMillis();
        long total = until - start;
        long state = cur - start;
        int progress = (int) (state * MAX_VALUE / total);
        return progress < MAX_VALUE ? progress : MAX_VALUE;
    }

}
