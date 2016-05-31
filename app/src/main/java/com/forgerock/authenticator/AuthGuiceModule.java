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
import android.content.Context;

import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.storage.ModelOpenHelper;
import com.forgerock.authenticator.storage.Settings;
import com.forgerock.authenticator.utils.TimeKeeper;
import com.google.inject.AbstractModule;

/**
 * Guice module for the ForgeRock Authenticator app.
 */
public class AuthGuiceModule extends AbstractModule {
    private final Context context;

    /**
     * Called when the Guice module is being created.
     * @param application The application that the Guice module is used for.
     */
    public AuthGuiceModule(Application application) {
        this.context = application.getApplicationContext();
    }

    @Override
    protected void configure() {
        bind(IdentityModel.class).toInstance(new ModelOpenHelper(context).getModel());
        bind(Settings.class).toInstance(new Settings(context));
    }
}
