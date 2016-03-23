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

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.forgerock.authenticator.baseactivities.BaseIdentityActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.notifications.NotificationAdapter;

/**
 * Page for viewing a list of Notifications relating to a mechanism.
 */
public class NotificationActivity extends BaseIdentityActivity { //TODO: change this to extend BaseMechanismActivity

    private NotificationAdapter notificationAdapter;
    private DataSetObserver dataSetObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifications);

        final Identity identity = getIdentity();
        assert identity != null;

        notificationAdapter = new NotificationAdapter(this);
        ((GridView) findViewById(R.id.grid)).setAdapter(notificationAdapter);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (notificationAdapter.getCount() == 0) {
                    findViewById(R.id.empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.empty).setVisibility(View.GONE);
                }
            }
        };
        notificationAdapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationAdapter.unregisterDataSetObserver(dataSetObserver);
    }
}
