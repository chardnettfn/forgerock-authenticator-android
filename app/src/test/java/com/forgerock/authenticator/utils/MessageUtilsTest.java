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

package com.forgerock.authenticator.utils;

import com.forgerock.authenticator.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MessageUtilsTest {

    private MockWebServer server;
    private final String TEST_COOKIE = "testCookie";

    @Before
    public void setup() {
        server = new MockWebServer();

    }

    @After
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldSendMessageCorrectly() throws Exception {
        server.enqueue(new MockResponse());
        server.start();

        HttpUrl baseUrl = server.url("/");

        MessageUtils messageUtils = new MessageUtils();
        messageUtils.respond(baseUrl.toString(), TEST_COOKIE, "dGVzdHNlY3JldA==",
                "testMessageId", new HashMap<String, Object>());

        RecordedRequest request = server.takeRequest();

        assertEquals("resource=1.0, protocol=1.0", request.getHeader("Accept-API-Version"));
        assertEquals(TEST_COOKIE, request.getHeader("Cookie"));

    }

    @Test(expected = IOException.class)
    public void shouldRejectEmptySecret() throws Exception {

        MessageUtils messageUtils = new MessageUtils();
        messageUtils.respond("http://example.com", "cookie", "",
                "testMessageId", new HashMap<String, Object>());

    }
}
