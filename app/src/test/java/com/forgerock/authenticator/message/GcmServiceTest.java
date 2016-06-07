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
package com.forgerock.authenticator.message;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.TestGuiceModule;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.storage.IdentityModel;

import org.apache.tools.ant.taskdefs.condition.Not;
import org.forgerock.json.jose.builders.JwtClaimsSetBuilder;
import org.forgerock.json.jose.builders.SignedJwtBuilderImpl;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.util.encode.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Matchers.any;
import static org.robolectric.Shadows.shadowOf;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;

/**
 * Example of testing a service using Robolectric.
 *
 * In cases where mocks must be used for tests, the following approach is recommended:
 * public static class TestGcmService extends GcmService {
 *   private static Logger logger;
 *   private static NotificationFactory notificationFactory;
 *   private static ContextService contextService;
 *
 *   public TestGcmService() {
 *     super(logger, notificationFactory, contextService); // This is a VisibleForTesting protected constructor.
 *   }
 *
 *   public static void configure(Logger loggerVal, NotificationFactory notificationFactoryVal, ContextService contextServiceVal) {
 *     logger = loggerVal;
 *     notificationFactory = notificationFactoryVal;
 *     contextService = contextServiceVal;
 *   }
 * }
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GcmServiceTest {

    private NotificationManager notificationManager;
    private Map<String, String> baseMessage;
    private IdentityModel identityModel;
    private GcmService service;
    private TestLogger logger;

    private static final String CORRECT_SECRET = "2afd55692b492e60df7e9c0b4f55b0492afd55692b492e60df7e9c0b4f55b049";
    private static final String INCORRECT_SECRET = "52e2563abe7d27f3476117ba2bc802a952e2563abe7d27f3476117ba2bc802a9";
    private static final String MECHANISM_UID_KEY = "u";
    private static final String CHALLENGE_KEY = "c";

    @Before
    public void setup() throws Exception {
        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), new TestGuiceModule());
        notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);

        identityModel = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(IdentityModel.class);

        baseMessage = new HashMap<>();
        baseMessage.put("messageId", "message-id");
        baseMessage.put(CHALLENGE_KEY, "challenge-value");
        baseMessage.put(MECHANISM_UID_KEY, "0");


        service = Robolectric.setupService(GcmService.class);
        logger = TestLoggerFactory.getTestLogger(GcmService.class);
    }

    @Test
    public void shouldTriggerNewActivityIntentOnMessageReceived() throws Exception{

        // Given
        Push push = generateMockMechanism("0");
        Notification notification = mock(com.forgerock.authenticator.notifications.Notification.class);
        given(notification.getMechanism()).willReturn(push);

        given(push.addNotification(any(com.forgerock.authenticator.notifications.Notification.NotificationBuilder.class)))
                .willReturn(notification);
        given(push.getOwner()).willReturn(mock(Identity.class));
        setupIdentityModel(push);

        // When
        service.onMessageReceived("sender", generateBundle("id", push.getSecret(), baseMessage));

        // Then
        assertEquals(1, shadowOf(notificationManager).getAllNotifications().size());
    }

    @Test
    public void shouldNotTriggerNewActivityIntentOnMessageReceivedWithNonExistingMechanism() throws Exception {

        // Given
        setupIdentityModel(null);

        // When
        service.onMessageReceived("sender", generateBundle("id", CORRECT_SECRET, baseMessage));

        // Then
        assertEquals(shadowOf(notificationManager).getAllNotifications().size(), 0);
    }

    @Test
    public void shouldNotTriggerNewActivityIntentOnMessageReceivedWithInvalidMechanism() throws Exception {

        // Given
        final Push push = generateMockMechanism("0");
        given(push.addNotification(any(com.forgerock.authenticator.notifications.Notification.NotificationBuilder.class))).willThrow(new InvalidNotificationException("Fake exception"));
        setupIdentityModel(push);

        // When
        service.onMessageReceived("sender", generateBundle("id", push.getSecret(), baseMessage));

        // Then
        assertEquals(shadowOf(notificationManager).getAllNotifications().size(), 0);
        Assert.assertTrue(logger.getLoggingEvents().contains(
                error("Received message mapped invalid Notification to Mechanism. Skipping...")));
    }

    @Test
    public void shouldNotTriggerNewActivityIntentOnJwtReceivedWithInvalidSecret() throws Exception {
        // Given
        final Push push = generateMockMechanism("0");
        setupIdentityModel(push);

        // When
        service.onMessageReceived("sender", generateBundle("id", INCORRECT_SECRET, baseMessage));

        // Then
        assertEquals(shadowOf(notificationManager).getAllNotifications().size(), 0);
        Assert.assertTrue(logger.getLoggingEvents().contains(
                error("Failed to validate jwt.")));
    }

    @Test
    public void shouldNotTriggerNewActivityIntentOnMessageReceivedWithNonJwt() throws Exception {
        // Given
        String messageId = "id";
        Bundle mockBundle = mock(Bundle.class);
        JSONObject message = new JSONObject();
        for (String key : baseMessage.keySet()) {
            message.put(key, baseMessage.get(key));
        }
        given(mockBundle.getString("message")).willReturn(message.toString()); // Formatted as json
        given(mockBundle.getString("messageId")).willReturn(messageId);

        // When
        service.onMessageReceived("sender", mockBundle);

        // Then
        assertEquals(shadowOf(notificationManager).getAllNotifications().size(), 0);
        Assert.assertTrue(logger.getLoggingEvents().contains(
                error("Failed to reconstruct JWT.")));
    }

    private Push generateMockMechanism(String mechanismUid) throws InvalidNotificationException {
        final Push push = mock(Push.class);
        given(push.getMechanismUID()).willReturn(mechanismUid);
        given(push.getSecret()).willReturn(CORRECT_SECRET);
        return push;
    }

    private void setupIdentityModel(Mechanism mechanism) {
        List<Mechanism> mechanismList = new ArrayList<>();
        if (mechanism != null) {
            mechanismList.add(mechanism);
        }
        given(identityModel.getMechanisms()).willReturn(mechanismList);
    }

    private Bundle generateBundle(String messageId, String base64Secret, Map<String, String> map) throws JSONException{
        Bundle mockBundle = mock(Bundle.class);
        String jwt = generateJwt(base64Secret, map);
        given(mockBundle.getString("message")).willReturn(jwt);
        given(mockBundle.getString("messageId")).willReturn(messageId);
        return mockBundle;
    }

    private String generateJwt(String base64Secret, Map<String, String> data) {
        JwtClaimsSetBuilder builder = new JwtClaimsSetBuilder();
        for (String key : data.keySet()) {
            builder.claim(key, data.get(key));
        }

        byte[] secret = Base64.decode(base64Secret);
        SigningHandler signingHandler = new SigningManager().newHmacSigningHandler(secret);
        SignedJwtBuilderImpl jwtBuilder = new SignedJwtBuilderImpl(signingHandler);
        jwtBuilder.claims(builder.build());
        jwtBuilder.headers().alg(JwsAlgorithm.HS256);
        return jwtBuilder.build();
    }
}