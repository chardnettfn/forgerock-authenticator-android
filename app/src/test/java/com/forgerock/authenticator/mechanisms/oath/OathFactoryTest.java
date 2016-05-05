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

import android.content.Context;

import com.forgerock.authenticator.identity.Identity;
import android.content.Context;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class OathFactoryTest {

    private OathFactory factory;
    private IdentityModel model;
    private int mechanismUID;

    @Before
    public void setUp() {
        Context context = mock(Context.class);
        model = mock(IdentityModel.class);
        IdentityDatabase identityDatabase = mock(IdentityDatabase.class);
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        given(model.getStorageSystem()).willReturn(identityDatabase);
        given(model.getNewMechanismUID()).willReturn("0");
        given(model.addIdentity(any(Identity.IdentityBuilder.class))).willAnswer(new Answer<Identity>() {
            @Override
            public Identity answer(InvocationOnMock invocation) throws Throwable {
                return ((Identity.IdentityBuilder) invocation.getArguments()[0]).build(model);
            }
        });

        factory = new OathFactory(context, model);
    }

    @Test
    public void shouldParseVersionOne() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        Oath token = (Oath) factory.createFromUri(uri);
        assertEquals(token.getType(), Oath.TokenType.TOTP);
        assertEquals(token.getOwner().getAccountName(), "user.0");
    }

    @Test
    public void shouldHandleDefaultVersion() throws Exception {
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====";
        Oath token = (Oath) factory.createFromUri(uri);
        assertEquals(token.getType(), Oath.TokenType.TOTP);
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
        Oath token = (Oath) factory.createFromUri(uri);
        Oath secondToken = (Oath) factory.restoreFromParameters(token.getVersion(), token.asMap())
                .setMechanismUID("dummy")
                .build(token.getOwner());

        assertEquals(secondToken.asMap(), token.asMap());
    }

    @Test
    public void optionStorageShouldReflectDifferences() throws Exception { //TODO move to Oath tests
        String uri = "otpauth://totp/Forgerock:user.0?secret=ONSWG4TFOQ=====&version=1";
        String secondUri = "otpauth://totp/Forgerock:user.0?secret=IOHEOSHIEF=====&version=1";
        Oath token = (Oath) factory.createFromUri(uri);
        Oath secondToken = (Oath) factory.createFromUri(secondUri);

        assertNotEquals(secondToken.asMap(), token.asMap());
    }


}
