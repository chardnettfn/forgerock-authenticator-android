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

package com.squareup.picasso;

import android.net.Uri;

import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for replacing the implementation of Picasso
 */
public class MockPicasso extends Picasso {
    public static List<Uri> loadedUris;

    private static Picasso basePicasso = new Builder(RuntimeEnvironment.application).build();

    /**
     * Uses the builder provided with the default picasso implementation to call into super() correctly
     */
    MockPicasso() {
        super(RuntimeEnvironment.application, basePicasso.dispatcher, basePicasso.cache, null,
                RequestTransformer.IDENTITY, null, basePicasso.stats,
                basePicasso.defaultBitmapConfig, false, false);
    }

    /**
     * Reset the singleton and its contents
     */
    public static void init() {
        singleton = new MockPicasso();
        loadedUris = new ArrayList<>();
    }

    @Override
    public RequestCreator load(Uri uri) {
        loadedUris.add(uri);
        return new RequestCreator(this, uri, 0);
    }




}
