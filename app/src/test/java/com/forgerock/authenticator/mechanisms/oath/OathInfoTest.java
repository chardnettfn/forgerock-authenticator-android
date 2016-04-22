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

import org.junit.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OathInfoTest {

    public String[] oathUrls() {
        return new String[] {
                "otpauth://hotp/WeightWatchers:Dave?digits=6&counter=0",
                "otpauth://totp/WeightWatchers:Dave?digits=6&counter=0"
        };
    }

    public String[] nonOathUrls() {
        return new String[] {
                "pushauth://hotp/WeightWatchers:Dave?digits=6&counter=0",
                "random://totp/WeightWatchers:Dave?digits=6&counter=0"
        };
    }

    @Test
    public void shouldRecogniseOathProtocol() {
        for (String url : oathUrls()) {
            assertTrue(new OathInfo().matchesURI(url));
        }
    }

    @Test
    public void shouldRejectNonOathProtocol() {
        for (String url : nonOathUrls()) {
            assertFalse(new OathInfo().matchesURI(url));
        }
    }
}
