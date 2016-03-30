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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forgerock.authenticator.ConfirmationSwipeBar;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.message.MessageConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import roboguice.activity.RoboActivity;

/**
 * Activity which allows the user to approve or reject a request for authentication that has been
 * provided by a message.
 */
public class PushAuthActivity extends RoboActivity {
    private static final Logger logger = LoggerFactory.getLogger(PushAuthActivity.class);
    private ConfirmationSwipeBar swipeToConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pushauth);

        String title = getIntent().getStringExtra(MessageConstants.TITLE);
        final String messageId = getIntent().getStringExtra(MessageConstants.MESSAGE_ID);
        String message = getIntent().getStringExtra(MessageConstants.MESSAGE);

        assert title != null;
        assert messageId != null;
        assert message != null;

        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);

        TextView messageView = (TextView) findViewById(R.id.message);
        messageView.setText(message);

        ImageButton button = (ImageButton) findViewById(R.id.cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleView.setText(title);

        swipeToConfirm = (ConfirmationSwipeBar) findViewById(R.id.slideToConfirm);
        swipeToConfirm.setListener(new ConfirmationSwipeBar.ConfirmationSwipeBarListener() {
            @Override
            public void onConfirm() {
                new RespondToMessageTask(messageId).execute();
            }
        });
    }

    /**
     * Class responsible for making a request to OpenAM to complete the request.
     */
    private class RespondToMessageTask extends AsyncTask<Void, Void, Integer> {
        private String messageId;

        /**
         * Creates the task, setting the message id it is associated with.
         * @param messageId The message id that is being responded to.
         */
        public RespondToMessageTask(String messageId) {
            this.messageId = messageId;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // Local code used
            int returnCode = 404;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://openam.example.com:8080/openam/json/push/gcm/message?_action=send");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                JSONObject message = new JSONObject();
                message.put("messageId", messageId);

                OutputStream os = connection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(message.toString());
                osw.flush();
                osw.close();
                returnCode = connection.getResponseCode();
            } catch (IOException | JSONException e) {
                logger.error("Response to server failed.", e);
            }
            finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            return returnCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == 200) {
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
