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
package com.forgerock.authenticator;

import com.forgerock.authenticator.mechanisms.TOTP.Token;
import com.forgerock.authenticator.mechanisms.TOTP.TokenFactory;
import com.forgerock.authenticator.utils.URIMappingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TokenFactoryTest {

    private TokenFactory factory;

    @BeforeMethod
    public void setUp() {
        factory = new TokenFactory();
    }

    @Test
    public void shouldParseVersionOne() throws URIMappingException {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        Token token = factory.get(uri);
        assertEquals(token.getType(), Token.TokenType.TOTP);
        assertEquals(token.getLabel(), "user.0");
    }

    @Test
    public void shouldHandleDefaultVersion() throws URIMappingException {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====";
        Token token = factory.get(uri);
        assertEquals(token.getType(), Token.TokenType.TOTP);
        assertEquals(token.getLabel(), "user.0");
    }
}
