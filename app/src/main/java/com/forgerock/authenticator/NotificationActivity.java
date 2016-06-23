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

import android.app.ActionBar;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.forgerock.authenticator.baseactivities.BaseMechanismActivity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.notifications.NotificationAdapter;
import com.forgerock.authenticator.storage.IdentityModelListener;

/**
 * Page for viewing a list of Notifications relating to a mechanism.
 */
public class NotificationActivity extends BaseMechanismActivity {

    private NotificationAdapter notificationAdapter;
    private DataSetObserver dataSetObserver;
    private IdentityModelListener listener;
    private Mechanism mechanism;
    private static final int UI_REFRESH_TIME_MILLIS = 15000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifications);

        mechanism = getMechanism();

        if (mechanism == null) {
            finish();
            return;
        }

        notificationAdapter = new NotificationAdapter(this, mechanism);
        final ExpandableListView list = (ExpandableListView) findViewById(R.id.notification_list);
        list.setAdapter(notificationAdapter);
        for (int i = 0; i < notificationAdapter.getGroupCount(); i++) {
            list.expandGroup(i);
        }
        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        list.post(new RefreshNotificationTime(list));

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (notificationAdapter.getGroupCount() == 0) {
                    findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    findViewById(R.id.empty).setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < notificationAdapter.getGroupCount(); i++) {
                    list.expandGroup(i);
                }
            }
        };
        notificationAdapter.registerDataSetObserver(dataSetObserver);

        listener = new IdentityModelListener() {
            @Override
            public void notificationChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notificationAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        identityModel.addListener(listener);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_notification);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
        if (notificationAdapter != null) {
            notificationAdapter.unregisterDataSetObserver(dataSetObserver);
        }
        identityModel.removeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notifications, menu);

        menu.findItem(R.id.action_clear_history).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mechanism.clearInactiveNotifications();
                return true;
            }
        });
        return true;
    }

    private class RefreshNotificationTime implements Runnable {

        private View view;

        public RefreshNotificationTime (View view) {
            this.view = view;
        }

        @Override
        public void run() {
            notificationAdapter.updateTimes();
            view.postDelayed(this, UI_REFRESH_TIME_MILLIS);
        }
    }
}
