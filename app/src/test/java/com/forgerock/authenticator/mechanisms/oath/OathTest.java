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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.utils.TimeKeeper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static com.forgerock.authenticator.storage.IdentityDatabaseTest.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class OathTest {
    private IdentityModel model;
    private Oath oath;
    private Identity identity;

    @Before
    public void setUp() throws MechanismCreationException {
        model = mock(IdentityModel.class);
        IdentityDatabase identityDatabase = mock(IdentityDatabase.class);
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        given(model.getIdentityDatabase()).willReturn(identityDatabase);
        given(model.addIdentity(any(Identity.IdentityBuilder.class))).willAnswer(new Answer<Identity>() {
            @Override
            public Identity answer(InvocationOnMock invocation) throws Throwable {
                return ((Identity.IdentityBuilder) invocation.getArguments()[0]).build(model);
            }
        });

        identity = Identity.builder().setIssuer("ForgeRock").setAccountName("test.user").build(model);
    }

    @Test
    public void shouldHandleHOTPCorrectlyWith6Digits() throws Exception {
        oath = (Oath) Oath.builder()
                .setAlgorithm("sha1")
                .setCounter("0")
                .setDigits("6")
                .setPeriod("30")
                .setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM")
                .setType("hotp")
                .setMechanismUID("0")
                .build(identity);

        assertEquals(oath.generateNextCode().getCurrentCode(), "785324");
        assertEquals(oath.generateNextCode().getCurrentCode(), "361422");
        assertEquals(oath.generateNextCode().getCurrentCode(), "054508");

        assertEquals(oath.getCounter(), 3);
    }

    @Test
    public void shouldHandleHOTPCorrectlyWith8Digits() throws Exception {
        oath = (Oath) Oath.builder()
                .setAlgorithm("sha1")
                .setCounter("0")
                .setDigits("8")
                .setPeriod("30")
                .setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM")
                .setType("hotp")
                .setMechanismUID("0")
                .build(identity);

        assertEquals(oath.generateNextCode().getCurrentCode(), "60785324");
        assertEquals(oath.generateNextCode().getCurrentCode(), "92361422");
        assertEquals(oath.generateNextCode().getCurrentCode(), "38054508");

        assertEquals(oath.getCounter(), 3);
    }

    @Test
    public void shouldProduceMapWithMinimumValuesSet() throws Exception {
        Map<String, String> map = Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("0").build(identity).asMap();
        assertNotEquals(map, null);
        assertEquals(map.size(), 6);
        for (String key : map.keySet()) {
            assertNotEquals(map.get(key), null);
        }
    }

    @Test
    public void shouldHandleTOTPCorrectly() throws Exception {

        TimeKeeper timeKeeper = new TimeKeeper() {
            long time = 1461773681957l;
            @Override
            public long getCurrentTimeMillis() {
                return time;
            }

            @Override
            public void timeTravel(long addTime) {
                time += addTime;
            }
        };

        oath = (Oath) Oath.builder()
                .setAlgorithm("sha1")
                .setCounter("0")
                .setDigits("6")
                .setPeriod("30")
                .setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM")
                .setType("totp")
                .setTimeKeeper(timeKeeper)
                .setMechanismUID("0")
                .build(identity);

        assertEquals(oath.generateNextCode().getCurrentCode(), "994721");
        timeKeeper.timeTravel(30000);
        assertEquals(oath.generateNextCode().getCurrentCode(), "589452");
        timeKeeper.timeTravel(30000);
        assertEquals(oath.generateNextCode().getCurrentCode(), "982313");

        assertEquals(oath.getCounter(), 0);
    }


}
