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
 * Portions Copyright 2014 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.add;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.squareup.picasso.Picasso;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.TOTP.TokenPersistence;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class AddActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final int SHA1_OFFSET = 1;
    private ImageButton mImage;
    private EditText mIssuer;
    private EditText mLabel;
    private EditText mSecret;
    private EditText mInterval;
    private EditText mCounter;
    private Spinner mAlgorithm;
    private RadioButton mHOTP;

    private Uri mImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        mImage = (ImageButton) findViewById(R.id.image);
        mIssuer = (EditText) findViewById(R.id.issuer);
        mLabel = (EditText) findViewById(R.id.label);
        mSecret = (EditText) findViewById(R.id.secret);
        mInterval = (EditText) findViewById(R.id.interval);
        mCounter = (EditText) findViewById(R.id.counter);
        mAlgorithm = (Spinner) findViewById(R.id.algorithm);
        mHOTP = (RadioButton) findViewById(R.id.hotp);

        // Select the default algorithm
        mAlgorithm.setSelection(SHA1_OFFSET);

        // Setup the Counter toggle
        mHOTP.setOnCheckedChangeListener(this);

        // Setup the buttons
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.add).setEnabled(false);
        mImage.setOnClickListener(this);

        // Set constraints on when the Add button is enabled
        TextWatcher tw = new AddTextWatcher(this);
        mIssuer.addTextChangedListener(tw);
        mLabel.addTextChangedListener(tw);
        mSecret.addTextChangedListener(new AddSecretTextWatcher(this));
        mInterval.addTextChangedListener(tw);

        // Don't permit screenshots since these might contain Base 32 encoded keys.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image:
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 0);
                break;

            case R.id.cancel:
                finish();
                break;

            case R.id.add:
                // Get the fields
                String issuer = Uri.encode(mIssuer.getText().toString());
                String label = Uri.encode(mLabel.getText().toString());
                String secret = Uri.encode(mSecret.getText().toString());
                String algorithm = mAlgorithm.getSelectedItem().toString().toLowerCase(Locale.US);
                int interval = Integer.parseInt(mInterval.getText().toString());
                int digits = ((RadioButton) findViewById(R.id.digits6)).isChecked() ? 6 : 8;

                // Create the URI
                String uri = String.format(Locale.US,
                        "otpauth://%sotp/%s:%s?secret=%s&algorithm=%s&digits=%d&period=%d",
                        mHOTP.isChecked() ? "h" : "t", issuer, label,
                        secret, algorithm, digits, interval);

                // Add optional parameters.
                if (mHOTP.isChecked()) {
                    int counter = Integer.parseInt(mCounter.getText().toString());
                    uri = uri.concat(String.format("&counter=%d", counter));
                }
                if (mImageURL != null) {
                    try {
                        String enc = URLEncoder.encode(mImageURL.toString(), "utf-8");
                        uri = uri.concat(String.format("&image=%s", enc));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                // Add the token
                if (TokenPersistence.addWithToast(this, uri) != null)
                    finish();

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        findViewById(R.id.counter_row).setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mImageURL = data.getData();
            Picasso.with(this)
                    .load(mImageURL)
                    .placeholder(R.drawable.forgerock_logo)
                    .into(mImage);
        }
    }
}
