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
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.authenticator;

import org.forgerock.authenticator.utils.URIMappingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TokenTest {
    private Token token;

    @BeforeMethod
    public void setUp() {
        token = new Token();
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidType() throws URIMappingException {
        token.setType("badger");
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidAlgorithm() throws URIMappingException {
        token.setAlgorithm("badger");
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidDigits() throws URIMappingException {
        token.setDigits("badger");
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidPeriod() throws URIMappingException {
        token.setPeriod("badger");
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidSecretDecode() throws URIMappingException {
        token.setSecret("11111");
    }

    @Test (expectedExceptions = URIMappingException.class)
    public void shouldValidateInvalidCounter() throws URIMappingException {
        token.setCounter("badger");
    }
}