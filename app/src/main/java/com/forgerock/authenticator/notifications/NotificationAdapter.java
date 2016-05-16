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
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.SortedList;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import roboguice.RoboGuice;

/**
 * Class for linking the complete list of Notifications with a series of layouts which display each one.
 */
public class NotificationAdapter extends BaseExpandableListAdapter {
    private Mechanism mechanism;
    private final LayoutInflater mLayoutInflater;
    private List<Notification> pendingList;
    private List<Notification> historyList;
    private List<NotificationLayout> layoutList;

    /**
     * Creates the adapter, and finds the data model.
     */
    public NotificationAdapter(Context context, Mechanism mechanism) {
        this.mechanism = mechanism;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pendingList = new SortedList<>();
        historyList = new SortedList<>();
        layoutList = new ArrayList<>();
        reloadData();
    }

    @Override
    public int getGroupCount() {
        if (pendingList.isEmpty() && historyList.isEmpty()) {
            return 0;
        }
        if (pendingList.isEmpty() || historyList.isEmpty()) {
            return 1;
        }
        return 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).size();
    }

    @Override
    public List<Notification> getGroup(int groupPosition) {
        if (groupPosition == 0) {
            if (pendingList.isEmpty()) {
                return historyList;
            }
            return pendingList;
        }
        return historyList;
    }

    @Override
    public Notification getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.notificationtitle, parent, false);
        }

        String title = "History";
        if (groupPosition == 0) {
            if (pendingList.isEmpty()) {
                title = "History";
            } else {
                title = "Pending";
            }
        }
        ((TextView) convertView.findViewById(R.id.heading_title)).setText(title);
        convertView.bringToFront();
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.notificationcell, parent, false);
        }

        Notification notification = getChild(groupPosition, childPosition);
        NotificationLayout layout = ((NotificationLayout) convertView);
        layout.bind(notification);
        layoutList.add(layout);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        reloadData();
        super.notifyDataSetChanged();
    }

    private void reloadData() {
        pendingList.clear();
        historyList.clear();
        layoutList.clear();
        for (Notification notification : mechanism.getNotifications()) {
            if (notification.isActive()) {
                pendingList.add(notification);
            } else {
                historyList.add(notification);
            }
        }
    }

    /**
     * Causes the adapter to update the current times on all Notifications it contains.
     */
    public void updateTimes() {
        long currentTimeUTCMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        boolean dataChanged = false;
        for (NotificationLayout layout : layoutList) {
            boolean wasActive = layout.isActive();
            layout.refresh(currentTimeUTCMillis);
            if (wasActive != layout.isActive()) {
                dataChanged = true;
            }
        }
        if (dataChanged) {
            notifyDataSetChanged();
        }
    }
}
