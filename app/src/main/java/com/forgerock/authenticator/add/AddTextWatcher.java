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
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.add;

import com.forgerock.authenticator.R;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;


public class AddTextWatcher implements TextWatcher {
    private final Button   mButton;
    private final EditText mIssuer;
    private final EditText mLabel;
    private final EditText mSecret;
    private final EditText mInterval;

    public AddTextWatcher(Activity activity) {
        mButton = (Button) activity.findViewById(R.id.add);
        mIssuer = (EditText) activity.findViewById(R.id.issuer);
        mLabel = (EditText) activity.findViewById(R.id.label);
        mSecret = (EditText) activity.findViewById(R.id.secret);
        mInterval = (EditText) activity.findViewById(R.id.interval);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mButton.setEnabled(false);

        if (mIssuer.getText().length() == 0)
            return;

        if (mLabel.getText().length() == 0)
            return;

        if (mSecret.getText().length() < 8)
            return;

        if (mInterval.getText().length() == 0)
            return;

        mButton.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
