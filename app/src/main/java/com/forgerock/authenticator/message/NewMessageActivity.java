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

package com.forgerock.authenticator.message;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.forgerock.authenticator.R;

/**
 * A new message activity is a ui activity that displayes the content of a message to the user.
 * At present the content of the message is held in the Intent object.  Intents are not secure so
 * when we switch to having messages that contain secure information we will need to save the
 * message locally and reference it by an id which we will pass thoguh the Intent.
 */
public class NewMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: securly store the message details and reference them by id
        // - intents are not secure

        // display the message stored in the intent
        setContentView(R.layout.activity_new_message);
        ((TextView) findViewById(R.id.message_title)).setText(getIntent().getStringExtra("title"));
        ((TextView) findViewById(R.id.message_content)).setText(getIntent().getStringExtra("message"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); // dispose of the message when navigating away
    }
}
