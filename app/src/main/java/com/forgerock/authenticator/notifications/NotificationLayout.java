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
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseNotificationActivity;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Individual entry which displays information about a given Notification.
 */
public class NotificationLayout extends FrameLayout {

    private Notification notification;
    private java.text.DateFormat dateFormat;
    private java.text.DateFormat timeFormat;
    private boolean isActive;

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     */
    public NotificationLayout(Context context) {
        super(context);
        setup(context);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     * @param defStyleAttr The resource containing default style attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    private void setup(Context context) {
        dateFormat = DateFormat.getMediumDateFormat(context);
        timeFormat = DateFormat.getTimeFormat(context);
    }

    /**
     * Set the Notification that this Layout displays.
     * @param notification The Notification to display.
     */
    void bind(final Notification notification) {
        this.notification = notification;
        final Context context = getContext();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseNotificationActivity.start(context, PushAuthActivity.class, notification);
            }
        });

        setClickable(notification.isActive());

        refresh();
    }

    /**
     * Determine if the notification was active last time this was refreshed.
     * @return The latest active status of the notification.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Update the current time and status, based on a millisecond value passed in.
     */
    public void refresh() {
        ImageView statusImage = (ImageView) findViewById(R.id.image);
        TextView statusText = (TextView) findViewById(R.id.status);
        if (notification.wasApproved()) {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_approved));
            statusText.setText(R.string.notification_status_approved);
        } else if (notification.isExpired() && notification.isPending()){
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_denied));
            statusText.setText(R.string.notification_status_expired);
        } else if (notification.isPending()) {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_pending));
            statusText.setText(R.string.notification_status_pending);
        } else {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_denied));
            statusText.setText(R.string.notification_status_rejected);
        }

        TextView timeView = (TextView) findViewById(R.id.time);
        timeView.setText(calendarToTimeString(notification.getTimeAdded()));

        this.isActive = notification.isActive();
    }

    private String calendarToTimeString(Calendar calendar) { //TODO: use String resources
        DateTime notificationTime = new DateTime(calendar.getTimeInMillis());

        DateTime midnight = DateTime.now().withTimeAtStartOfDay();

        if (notificationTime.isAfter(midnight)) {
            return timeFormat.format(notificationTime.getMillis());
        }
        else if (notificationTime.isAfter(midnight.minusDays(1))) {
            return getContext().getString(R.string.time_yesterday);
        } else if (notificationTime.isAfter(midnight.minusDays(7))) {
            switch (notificationTime.getDayOfWeek()) {
                case 1: return getContext().getString(R.string.time_monday);
                case 2: return getContext().getString(R.string.time_tuesday);
                case 3: return getContext().getString(R.string.time_wednesday);
                case 4: return getContext().getString(R.string.time_thursday);
                case 5: return getContext().getString(R.string.time_friday);
                case 6: return getContext().getString(R.string.time_saturday);
                case 7: return getContext().getString(R.string.time_sunday);
            }
        }
        return dateFormat.format(notificationTime.getMillis());
    }
}
