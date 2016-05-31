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

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.test.InstrumentationRegistry;

import com.jraska.falcon.FalconSpoon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Contains utility methods for acting on a device.
 */
public class TestDevice {

    /**
     * Take a screenshot using UiAutomation#takeScreenshot, falling back to FalconSpoon on lower API versions.
     */
    public static File screenshot(Activity activity, String tag) {
        File file = FalconSpoon.screenshot(activity, tag);
        if (Build.VERSION.SDK_INT >= 18) {
            Bitmap bmp = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            if (bmp == null) {
                return file;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return file;
    }
}
