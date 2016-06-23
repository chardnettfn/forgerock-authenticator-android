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

package com.forgerock.authenticator.mechanisms.push;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.baseactivities.BaseNotificationActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.ui.ConfirmationSwipeBar;
import com.forgerock.authenticator.R;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity which allows the user to approve or reject a request for authentication that has been
 * provided by a message.
 */
public class PushAuthActivity extends BaseNotificationActivity {
    private static final Logger logger = LoggerFactory.getLogger(PushAuthActivity.class);
    private ConfirmationSwipeBar swipeToConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pushauth);

        final Notification notification = getNotification();

        if (notification == null || !notification.isActive()) {
            finish();
            return;
        }

        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(String.format(getString(R.string.pushauth_login_title), notification.getMechanism().getOwner().getIssuer()));

        ImageButton cancelButton = (ImageButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DenyNotificationTask(notification).execute();
            }
        });

        Identity owner = notification.getMechanism().getOwner();
        try {
            String color = owner.getBackgroundColor();
            if (color != null) {
                findViewById(R.id.header).setBackgroundColor(Color.parseColor(color));
            }
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        ImageView imageView = (ImageView) findViewById(R.id.image);

        Picasso.with(this)
                .load(owner.getImageURL())
                .placeholder(R.drawable.forgerock_placeholder)
                .into(imageView);

        swipeToConfirm = (ConfirmationSwipeBar) findViewById(R.id.slideToConfirm);
        swipeToConfirm.setListener(new ConfirmationSwipeBar.ConfirmationSwipeBarListener() {
            @Override
            public void onConfirm() {
                new AcceptNotificationTask(notification).execute();
            }
        });
    }

    /**
     * Class responsible for making a request to OpenAM to complete the authentication request.
     */
    private abstract class RespondToMessageTask extends AsyncTask<Void, Void, Boolean> {
        protected Notification notification;

        /**
         * Creates the task, setting the message id it is associated with.
         * @param notification The notification that is being responded to.
         */
        public RespondToMessageTask(Notification notification) {
            this.notification = notification;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                new AlertDialog.Builder(PushAuthActivity.this)
                        .setTitle(R.string.pushauth_fail_title)
                        .setMessage(R.string.notification_error_network_failure_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                swipeToConfirm.unselect();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                finish();
            }
        }
    }

    private class AcceptNotificationTask extends RespondToMessageTask {
        /**
         * Creates the task, setting the message id it is associated with.
         * @param notification The notification that is being responded to.
         */
        public AcceptNotificationTask(Notification notification) {
            super(notification);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return notification.accept();
        }
    }

    private class DenyNotificationTask extends RespondToMessageTask {
        /**
         * Creates the task, setting the message id it is associated with.
         * @param notification The notification that is being responded to.
         */
        public DenyNotificationTask(Notification notification) {
            super(notification);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return notification.deny();
        }
    }
}
