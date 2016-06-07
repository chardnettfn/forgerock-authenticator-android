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

package com.forgerock.authenticator.notifications;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.TestGuiceModule;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.support.MockIdentityBuilder;
import com.forgerock.authenticator.utils.MessageUtils;

import org.forgerock.util.encode.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Random;

import roboguice.RoboGuice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PushNotificationTest {

    private IdentityModel model;
    private IdentityDatabase identityDatabase;
    private Identity identity;
    private Push push;
    private PushNotification notification;
    private MessageUtils messageUtils;

    @Before
    public void setUp() throws Exception {

        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), new TestGuiceModule());

        model = mock(IdentityModel.class);
        identityDatabase = mock(IdentityDatabase.class);
        given(model.getStorageSystem()).willReturn(identityDatabase);
        given(model.getInjector()).willReturn(RoboGuice.getInjector(RuntimeEnvironment.application));

        messageUtils = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MessageUtils.class);

        identity = new MockIdentityBuilder().withIssuer("TestIssuer").withAccountName("test.user").build();

        byte[] random = new byte[32];
        new Random().nextBytes(random);
        String base64value = Base64.encode(random);


        push = mock(Push.class);
        given(push.getModel()).willReturn(model);
        given(push.getSecret()).willReturn(base64value);

        notification = (PushNotification) PushNotification.builder().setId(1).build(push);

    }

    @Test
    public void shouldSaveCorrectly() throws Exception {
        notification = (PushNotification) PushNotification.builder().build(push);

        notification.save();
        verify(identityDatabase).addNotification(notification);
    }

    @Test
    public void shouldUpdateCorrectly() throws Exception {
        notification.save();
        verify(identityDatabase).updateNotification(1, notification);
    }

    @Test
    public void shouldForceSaveCorrectly() throws Exception {
        notification.forceSave();
        verify(identityDatabase).addNotification(notification);
    }

    @Test
    public void shouldDeleteCorrectly() throws Exception {
        notification.delete();
        verify(identityDatabase).deleteNotification(1);
    }

    @Test
    public void shouldAcceptCorrectlyWithCorrectServer() throws Exception {
        given(messageUtils.respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class))).willReturn(200);
        notification.accept();
        verify(messageUtils).respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class));
        verify(identityDatabase).updateNotification(1, notification);
        assertTrue(notification.wasApproved());
        assertFalse(notification.isPending());
    }

    @Test
    public void shouldFailToAcceptWithIncorrectServer() throws Exception {
        given(messageUtils.respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class))).willReturn(404);
        notification.accept();
        verify(messageUtils).respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class));
        verifyZeroInteractions(identityDatabase);
        assertFalse(notification.wasApproved());
        assertTrue(notification.isPending());
    }

    @Test
    public void shouldDenyCorrectlyWithCorrectServer() throws Exception {
        given(messageUtils.respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class))).willReturn(200);
        notification.deny();
        verify(messageUtils).respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class));
        verify(identityDatabase).updateNotification(1, notification);
        assertFalse(notification.wasApproved());
        assertFalse(notification.isPending());

    }

    @Test
    public void shouldFailToDenyWithIncorrectServer() throws Exception {
        given(messageUtils.respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class))).willReturn(404);
        notification.deny();
        verify(messageUtils).respond(anyString(), anyString(), anyString(), anyString(), anyMapOf(String.class, Object.class));
        verifyZeroInteractions(identityDatabase);
        assertFalse(notification.wasApproved());
        assertTrue(notification.isPending());

    }
}
