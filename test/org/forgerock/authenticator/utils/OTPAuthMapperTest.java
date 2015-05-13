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
package org.forgerock.authenticator.utils;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class OTPAuthMapperTest {

    private OTPAuthMapper mapper;

    @BeforeMethod
    public void setUp() {
        mapper = new OTPAuthMapper();
    }

    @Test
    public void shouldParseType() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://hotp/Example:alice@gmail.com");
        assertEquals(result.get(OTPAuthMapper.TYPE), "hotp");
    }

    @Test
    public void shouldParseAccountName() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://hotp/example");
        assertEquals(result.get(OTPAuthMapper.LABEL), "example");
    }

    @Test
    public void shouldParseIssuerFromPath() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://hotp/Badger:ferret");
        assertEquals(result.get(OTPAuthMapper.ISSUER), "Badger");
    }

    @Test
    public void shouldOverwriteIssuerFromParamters() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://hotp/Badger:ferret?issuer=Stoat");
        assertEquals(result.get(OTPAuthMapper.ISSUER), "Stoat");
    }

    @Test
    public void shouldHandleMissingQueryParameters() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://totp/Example:alice@google.com");
        assertEquals(result.get("missing"), null);
    }

    @Test
    public void shouldParseKnownQueryParameters() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP");
        assertEquals(result.get(OTPAuthMapper.SECRET), "JBSWY3DPEHPK3PXP");
    }

    @Test
    public void shouldParseUnspecifiedQueryParameters() throws URIMappingException {
        Map<String, String> result = mapper.map("otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP&badger=ferret");
        assertEquals(result.get("badger"), "ferret");
    }
}