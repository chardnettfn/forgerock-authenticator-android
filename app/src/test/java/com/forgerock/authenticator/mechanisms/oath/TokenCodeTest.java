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

package com.forgerock.authenticator.mechanisms.oath;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.utils.TimeKeeper;

import org.junit.Before;
import org.junit.Test;

import static org.testng.Assert.assertEquals;

public class TokenCodeTest {

    private final String CODE = "CODE";
    private final long EXPIRY_DELAY = 30000;
    private TokenCode tokenCode;
    private TimeKeeper timeKeeper;

    @Before
    public void setUp() throws MechanismCreationException {
        timeKeeper = new TimeKeeper() {
            private long offset = 0;

            @Override
            public long getCurrentTimeMillis() {
                return System.currentTimeMillis() + offset;
            }

            @Override
            public void timeTravel(long addTime) {
                offset += addTime;
            }
        };
        tokenCode = new TokenCode(timeKeeper, CODE, System.currentTimeMillis(), System.currentTimeMillis() + EXPIRY_DELAY);
    }

    @Test
    public void shouldContainCorrectCode() throws Exception {
        assertEquals(tokenCode.getCurrentCode(), CODE);
    }

    @Test
    public void shouldReportCorrectValidityBeforeAndAfterExpiry() throws Exception {
        assertEquals(tokenCode.isValid(), true);
        timeKeeper.timeTravel(EXPIRY_DELAY);
        assertEquals(tokenCode.isValid(), false);
    }

    @Test
    public void shouldReportProgressBeforeExpiry() throws Exception {
        assertEquals(tokenCode.getCurrentProgress(), 0);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(tokenCode.getCurrentProgress(), 250);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(tokenCode.getCurrentProgress(), 500);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(tokenCode.getCurrentProgress(), 750);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(tokenCode.getCurrentProgress(), 1000);
    }

    @Test
    public void shouldReportFullProgressAfterExpiry() throws Exception {
        timeKeeper.timeTravel(EXPIRY_DELAY * 2);
        assertEquals(tokenCode.getCurrentProgress(), 1000);
    }
}
