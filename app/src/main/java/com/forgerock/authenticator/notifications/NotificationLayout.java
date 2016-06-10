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
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseNotificationActivity;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Individual entry which displays information about a given Notification.
 */
public class NotificationLayout extends FrameLayout {

    private Notification notification;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");;
    private boolean isActive;

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     */
    public NotificationLayout(Context context) {
        super(context);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create a cell that a Notification is displayed in.
     * @param context The context that the Notification is called from.
     * @param attrs The list of attributes.
     * @param defStyleAttr The resource containing default style attributes.
     */
    public NotificationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        refresh(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
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
     * @param currentTimeUTCMillis The current UTC time in ms.
     */
    public void refresh(long currentTimeUTCMillis) {
        ImageView statusImage = (ImageView) findViewById(R.id.image);
        TextView statusText = (TextView) findViewById(R.id.status);
        if (notification.wasApproved()) {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_approved));
            statusText.setText("Approved");
        } else if (notification.isExpired() && notification.isPending()){
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_denied));
            statusText.setText("Expired");
        } else if (notification.isPending()) {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_pending));
            statusText.setText("Pending");
        } else {
            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.forgerock_icon_denied));
            statusText.setText("Denied");
        }

        TextView timeView = (TextView) findViewById(R.id.time);
        timeView.setText(calendarToTimeString(currentTimeUTCMillis, notification.getTimeAdded()));

        this.isActive = notification.isActive();
    }

    private String calendarToTimeString(long currentTimeUTCMillis, Calendar calendar) { //TODO: use String resources
        long timeDiffMillis = currentTimeUTCMillis - calendar.getTimeInMillis();

        if (TimeUnit.MILLISECONDS.toSeconds(timeDiffMillis) < 60) {
            return "Less than 1 minute ago";
        }
        else if (TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) == 1) {
            return "1 minute ago";
        } else if (TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) < 60) {
            return TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) + " minutes ago";
        } else if (TimeUnit.MILLISECONDS.toHours(timeDiffMillis) < 24) {
            return TimeUnit.MILLISECONDS.toHours(timeDiffMillis) + " hours ago";
        } else if (TimeUnit.MILLISECONDS.toDays(timeDiffMillis) == 1) {
            return "Yesterday";
        } else if (TimeUnit.MILLISECONDS.toDays(timeDiffMillis) < 7) {
            return TimeUnit.MILLISECONDS.toDays(timeDiffMillis) + " days ago";
        }

        return format.format(calendar.getTime());
    }
}
