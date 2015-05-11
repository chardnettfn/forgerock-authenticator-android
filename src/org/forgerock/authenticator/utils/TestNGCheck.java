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

/**
 * A simplistic way to verify that TestNG has not been included in the
 * compiled application.
 */
public class TestNGCheck {
    /**
     * @return True if TestNG is on the classpath.
     */
    public static boolean isTestNGOnClassPath() {
        try {
            Class.forName("org.testng.annotations.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
