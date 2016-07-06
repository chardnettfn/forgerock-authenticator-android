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

package com.forgerock.authenticator.mechanisms.push;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class PushTest {
    private IdentityModel model;
    private Identity identity;

    @Before
    public void setUp() throws MechanismCreationException {
        model = mock(IdentityModel.class);
        IdentityDatabase identityDatabase = mock(IdentityDatabase.class);
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        given(model.getStorageSystem()).willReturn(identityDatabase);
        given(model.addIdentity(any(Identity.IdentityBuilder.class))).willAnswer(new Answer<Identity>() {
            @Override
            public Identity answer(InvocationOnMock invocation) throws Throwable {
                return ((Identity.IdentityBuilder) invocation.getArguments()[0]).build(model);
            }
        });

        identity = Identity.builder().setIssuer("ForgeRock").setAccountName("test.user").build(model);
    }

    @Test
    public void shouldNotAllowEmptySecret() throws Exception {
        try {
            identity.addMechanism(Push.builder().setMechanismUID("1"));
            fail("Should throw an exception");
        } catch (MechanismCreationException e) {
            assertEquals(e.getMessage(), "Secret was null or empty");
        }

    }

}
