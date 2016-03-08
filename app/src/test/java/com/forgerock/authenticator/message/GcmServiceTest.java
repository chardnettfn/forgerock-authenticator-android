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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.forgerock.authenticator.utils.ContextService;
import com.forgerock.authenticator.utils.IntentFactory;
import com.forgerock.authenticator.utils.NotificationFactory;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

public class GcmServiceTest {

    private Logger mockLogger;

    @BeforeMethod
    public void setup() {
        mockLogger = mock(Logger.class);
    }

    @Test
    public void shouldTriggerNewActivityIntentOnMessageReceived() {
        // Given
        String message = "badger";

        NotificationManager mockNotificationManager = mock(NotificationManager.class);

        GcmService service = new GcmService(
                mockLogger,
                generateIntentFactory(mock(Intent.class)),
                generateFactory(mock(Notification.class)),
                generateContextService(mockNotificationManager));

        // When

        service.onMessageReceived(message, generateBundle("badger"));

        // Then
        verify(mockNotificationManager).notify(anyInt(), any(Notification.class));
    }

    private static IntentFactory generateIntentFactory(Intent mockToReturn) {
        IntentFactory mock = mock(IntentFactory.class);
        given(mock.generateInternal(any(Context.class), any(Class.class))).willReturn(mockToReturn);
        return mock;
    }

    private static NotificationFactory generateFactory(Notification mockToReturn) {
        NotificationFactory mock = mock(NotificationFactory.class);
        given(mock.generatePending(any(Context.class), anyInt(), anyString(), anyString(), any(Intent.class)))
                .willReturn(mockToReturn);
        return mock;
    }

    private static ContextService generateContextService(NotificationManager mockToReturn) {
        ContextService mock = mock(ContextService.class);
        given(mock.getService(any(Context.class), anyString())).willReturn(mockToReturn);
        return mock;
    }

    private static Bundle generateBundle(String key) {
        Bundle mockBundle = mock(Bundle.class);
        given(mockBundle.getString(anyString())).willReturn(key);
        return mockBundle;
    }
}