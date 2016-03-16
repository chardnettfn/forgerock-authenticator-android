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
 * Copyright 2015-2016 ForgeRock AS.
 */

package com.forgerock.authenticator.edit;

import android.os.Bundle;

import com.forgerock.authenticator.storage.IdentityDatabase;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

public abstract class BaseActivity extends RoboActivity {
    public static final String ROW_ID = "rowid";
    private long rowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the position of the token. This MUST exist.
        rowId = getIntent().getLongExtra(ROW_ID, -1);
        assert rowId >= 0;
    }

    public IdentityDatabase getIdentityDatabase() {
        return RoboGuice.getInjector(this).getInstance(IdentityDatabase.class);
    }

    protected long getRowId() {
        return rowId;
    }


}
