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

package com.forgerock.authenticator.identity;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;
import static uk.org.lidalia.slf4jtest.LoggingEvent.info;

public class IdentityBuilderTest {
    private IdentityModel model;
    private final String ISSUER = "test.issuer";
    private final String ACCOUNT_NAME = "test.user";

    @Before
    public void setUp() throws MechanismCreationException {
        model = mock(IdentityModel.class);
        IdentityDatabase identityDatabase = mock(IdentityDatabase.class);
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        given(model.getIdentityDatabase()).willReturn(identityDatabase);
    }

    @Test
    public void createsValuesCorrectly() throws Exception {
        Identity identity = Identity.builder()
                .setIssuer(ISSUER)
                .setAccountName(ACCOUNT_NAME)
                .build(model);
        assertEquals(identity.getIssuer(), ISSUER);
        assertEquals(identity.getAccountName(), ACCOUNT_NAME);
    }

    @Test
    public void shouldSetCorrectDefaultValuesWhenNotSet() throws Exception {
        Identity identity = Identity.builder()
                .build(model);
        assertEquals(identity.getIssuer(), "");
        assertEquals(identity.getAccountName(), "");
    }

    @Test
    public void shouldSetCorrectDefaultValuesWhenSetToNull() throws Exception {
        Identity identity = Identity.builder()
                .setIssuer(null)
                .setAccountName(null)
                .build(model);
        assertEquals(identity.getIssuer(), "");
        assertEquals(identity.getAccountName(), "");
    }

    @Test
    public void createsMechanismsCorrectly() throws Exception {
        List<Mechanism.PartialMechanismBuilder> builderList = new ArrayList<>();
        builderList.add(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setId(0).setMechanismUID("0"));
        builderList.add(Push.builder().setId(1).setMechanismUID("1"));
        Identity identity = Identity.builder()
                .setMechanisms(builderList)
                .build(model);

        assertEquals(identity.getMechanisms().size(), 2);
        assertEquals(identity.getMechanisms().get(0).getOwner(), identity);
        assertEquals(identity.getMechanisms().get(1).getOwner(), identity);
    }

    @Test
    public void rejectUnstoredMechanism() throws Exception {
        List<Mechanism.PartialMechanismBuilder> builderList = new ArrayList<>();
        builderList.add(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("0"));
        TestLogger logger = TestLoggerFactory.getTestLogger(Identity.class);

        assertEquals(logger.getAllLoggingEvents().size(), 0);
        Identity.builder()
                .setMechanisms(builderList)
                .build(model);

        assertEquals(logger.getAllLoggingEvents().size(), 1);
        assertEquals(logger.getLoggingEvents().get(0),
                error("Tried to populate mechanism list with Mechanism that has not been stored."));
    }

}
