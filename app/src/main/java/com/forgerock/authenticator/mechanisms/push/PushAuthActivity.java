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
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forgerock.authenticator.baseactivities.BaseNotificationActivity;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.ui.ConfirmationSwipeBar;
import com.forgerock.authenticator.R;

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
        final Context context = this;

        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText("Log into " + notification.getMechanism().getOwner().getIssuer() + "?");

        ImageButton cancelButton = (ImageButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notification.deny(context);
                finish();
            }
        });

        swipeToConfirm = (ConfirmationSwipeBar) findViewById(R.id.slideToConfirm);
        swipeToConfirm.setListener(new ConfirmationSwipeBar.ConfirmationSwipeBarListener() {
            @Override
            public void onConfirm() {
                new RespondToMessageTask(context, notification).execute();
            }
        });
    }

    /**
     * Class responsible for making a request to OpenAM to complete the authentication request.
     */
    private class RespondToMessageTask extends AsyncTask<Void, Void, Boolean> {
        private Notification notification;
        private Context context;

        /**
         * Creates the task, setting the message id it is associated with.
         * @param notification The notification that is being responded to.
         */
        public RespondToMessageTask(Context context, Notification notification) {
            this.notification = notification;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return notification.accept(context);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                new AlertDialog.Builder(PushAuthActivity.this)
                        .setTitle("Error")
                        .setMessage("Failed to connect to server")
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
}
