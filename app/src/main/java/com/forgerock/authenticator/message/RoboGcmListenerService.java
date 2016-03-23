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

import com.forgerock.authenticator.storage.IdentityModel;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.inject.Injector;
import com.google.inject.Key;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.util.RoboContext;

/**
 * Extension of GcmListenerService to provide access to the RoboGuice injector.
 */
public class RoboGcmListenerService extends GcmListenerService implements RoboContext {

    protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>, Object>();
    protected IdentityModel identityModel;

    @Override
    public void onCreate() {
        final Injector injector = RoboGuice.getInjector(this);
        injector.injectMembers(this);
        super.onCreate();
        identityModel = RoboGuice.getInjector(this).getInstance(IdentityModel.class);
    }

    @Override
    public void onDestroy() {
        try {
            RoboGuice.destroyInjector(this);
        } finally {
            super.onDestroy();
        }
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }
}
