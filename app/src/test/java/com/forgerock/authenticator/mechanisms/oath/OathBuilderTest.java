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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.storage.IdentityModel;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class OathBuilderTest {
    private Oath.OathBuilder oathBuilder;

    @Before
    public void setUp() throws Exception {
        oathBuilder = Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1");
    }

    /**
     * Test that the default values are respected, as defined by https://github.com/google/google-authenticator/wiki/Key-Uri-Format.
     */
    @Test
     public void shouldPopulateWithCorrectDefaultValues() throws Exception {
        Identity identity = Identity.builder().setIssuer("ForgeRock").setAccountName("test.user").build(mock(IdentityModel.class));
        Oath oath = (Oath) oathBuilder.build(identity);
        assertEquals(oath.getDigits(), 6);
        assertEquals(oath.getPeriod(), 30);
        assertEquals(oath.getAlgo(), "SHA1");
    }

    @Test(expected = MechanismCreationException.class)
    public void shouldValidateInvalidType() throws MechanismCreationException {
        oathBuilder.setType("badger");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateInvalidAlgorithm() throws MechanismCreationException {
        oathBuilder.setAlgorithm("badger");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateNonNumberDigits() throws MechanismCreationException {
        oathBuilder.setDigits("badger");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateInvalidDigits() throws MechanismCreationException {
        oathBuilder.setDigits("7");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateInvalidPeriod() throws MechanismCreationException {
        oathBuilder.setPeriod("badger");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateInvalidSecretDecode() throws MechanismCreationException {
        oathBuilder.setSecret("11111");
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateNullSecretDecode() throws MechanismCreationException {
        oathBuilder.setSecret(null);
    }

    @Test (expected = MechanismCreationException.class)
    public void shouldValidateInvalidCounter() throws MechanismCreationException {
        oathBuilder.setCounter("badger");
    }
}