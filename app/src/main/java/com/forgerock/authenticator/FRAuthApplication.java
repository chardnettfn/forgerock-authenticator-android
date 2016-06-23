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

package com.forgerock.authenticator;

import android.app.Application;
import android.widget.Toast;

import com.forgerock.authenticator.utils.TestNGCheck;

import roboguice.RoboGuice;

/**
 * Used to disable annotation database for RoboGuice. Specifically, issues were encountered using
 * RoboGuice without RoboBlender.
 * {@link https://github.com/roboguice/roboguice/wiki/RoboBlender-wiki pro} provides
 * details on how to disable RoboBlender support which should be investigated at a later date.
 * TODO: AME-10125
 */
public class FRAuthApplication extends Application {
    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Notify developer that they have included TestNG on classpath.
        // TODO: AME-9927 should update or resolve this check
        if (TestNGCheck.isTestNGOnClassPath()) {
            Toast.makeText(getApplicationContext(), R.string.compiled_with_test_libraries, Toast.LENGTH_LONG).show();
        }
    }


}
