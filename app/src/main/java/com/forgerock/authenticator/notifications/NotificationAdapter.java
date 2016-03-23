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

package com.forgerock.authenticator.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.List;

import roboguice.RoboGuice;

/**
 * Class for linking the complete list of Notifications with a series of layouts which display each one.
 */
public class NotificationAdapter extends BaseAdapter {
    private final IdentityModel identityModel;
    private final LayoutInflater mLayoutInflater;
    private List<Notification> notificationList;

    /**
     * Creates the adapter, and finds the data model.
     */
    public NotificationAdapter(Context context) {
        identityModel = RoboGuice.getInjector(context).getInstance(IdentityModel.class);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notificationList = identityModel.getNotifications();
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Notification getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.notificationcell, parent, false);
        }

        Notification not = getItem(position);
        ((NotificationLayout) convertView).bind(not);
        return convertView;
    }
}
