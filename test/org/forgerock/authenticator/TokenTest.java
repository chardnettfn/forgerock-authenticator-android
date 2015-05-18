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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TokenTest {
    @Test
    public void shouldParseIssuer() throws Token.TokenUriInvalidException {
        String token = "otpauth://hotp/Forgerock:user.0?secret=ONSWG4TFOQ=====&counter=1";
        assertEquals(new Token(token).getIssuer(), "Forgerock");
    }

    @Test
    public void shouldParseIssuerFromParameter() throws Token.TokenUriInvalidException {
        String token = "otpauth://hotp/user.0?secret=ONSWG4TFOQ=====&issuer=Forgerock&counter=1";
        assertEquals(new Token(token).getIssuer(), "Forgerock");
    }

    @Test
    public void shouldNotParseIssuerIfMissing() throws Token.TokenUriInvalidException {
        String token = "otpauth://hotp/user.0?secret=ONSWG4TFOQ=====&counter=1";
        assertEquals(new Token(token).getIssuer(), "");
    }

    @Test
    public void shouldParseLabel() throws Token.TokenUriInvalidException {
        String token = "otpauth://totp/user.0?secret=ABC";
        assertEquals(new Token(token).getLabel(), "user.0");
    }

    @Test
    public void shouldParseType()  throws Token.TokenUriInvalidException {
        String token = "otpauth://totp/user.0?secret=ABC";
        assertEquals(new Token(token).getType(), Token.TokenType.TOTP);
    }

    @Test (expectedExceptions = Token.TokenUriInvalidException.class)
    public void shouldErrorOnMissingType() throws Token.TokenUriInvalidException {
        new Token("otpauth://Forgerock:user.0");
    }

    @Test
    public void shouldParseDigitsDefault()  throws Token.TokenUriInvalidException {
        String token = "otpauth://totp/user.0?secret=ABC";
        assertEquals(new Token(token).getDigits(), 6);
    }

    @Test
    public void shouldParseDigits() throws Token.TokenUriInvalidException {
        String token = "otpauth://totp/Forgerock:user.0?secret=ABC&digits=8";
        assertEquals(new Token(token).getDigits(), 8);
    }
}