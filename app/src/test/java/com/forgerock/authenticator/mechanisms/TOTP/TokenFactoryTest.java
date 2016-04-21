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
package com.forgerock.authenticator.mechanisms.oath;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TokenFactoryTest {

    private TokenFactory factory;

    @BeforeMethod
    public void setUp() {
        factory = new TokenFactory();
    }

    @Test
    public void shouldParseVersionOne() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        Token token = (Token) factory.createFromUri(uri);
        assertEquals(token.getType(), Token.TokenType.TOTP);
        assertEquals(token.getOwner().getAccountName(), "user.0");
    }

    @Test
    public void shouldHandleDefaultVersion() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====";
        Token token = (Token) factory.createFromUri(uri);
        assertEquals(token.getType(), Token.TokenType.TOTP);
        assertEquals(token.getOwner().getAccountName(), "user.0");
    }

    @Test
    public void shouldRejectInvalidVersion() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=99999";
        try {
            factory.createFromUri(uri);
            fail("Should throw MechanismCreationException");
        } catch (Exception e) {
            assertTrue(e instanceof MechanismCreationException);
        }
    }

    @Test
    public void optionStorageShouldBeRepeatable() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        Oath token = (Oath) factory.createFromUri(mock(Context.class), uri);
        Oath secondToken = (Oath) factory.restoreFromParameters(token.getVersion(), token.asMap())
                .build(token.getOwner());

        assertEquals(secondToken.asMap(), token.asMap());
    }

    @Test
    public void optionStorageShouldReflectDifferences() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        String secondUri = "otpauth://totp/Forgerock:user.0?secret=IOHEOSHIEF=====&version=1";
        Token token = (Token) factory.createFromUri(uri);
        Token secondToken = (Token) factory.createFromUri(secondUri);

        assertNotEquals(secondToken.asMap(), token.asMap());
    }


}
