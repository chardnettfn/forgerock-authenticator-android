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

package com.forgerock.authenticator.edit;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.Token;
import com.forgerock.authenticator.TokenPersistence;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;

public class EditActivity extends BaseActivity implements TextWatcher, View.OnClickListener {
    private EditText           mIssuer;
    private EditText           mLabel;
    private ImageButton        mImage;
    private Button             mRestore;
    private Button             mSave;

    private String mIssuerCurrent;
    private String mIssuerDefault;
    private String mLabelCurrent;
    private String mLabelDefault;
    private Uri mImageCurrent;
    private Uri mImageDefault;
    private Uri mImageDisplay;

    private void showImage(Uri uri) {
        mImageDisplay = uri;
        onTextChanged(null, 0, 0, 0);
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.forgerock_logo)
                .into(mImage);
    }

    private boolean imageIs(Uri uri) {
        if (uri == null)
            return mImageDisplay == null;

        return uri.equals(mImageDisplay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);

        // Get token values.
        Token token = new TokenPersistence(this).get(getPosition());
        mIssuerCurrent = token.getIssuer();
        mLabelCurrent = token.getLabel();
        mImageCurrent = token.getImage();
        token.setIssuer(null);
        token.setLabel(null);
        token.setImage(null);
        mIssuerDefault = token.getIssuer();
        mLabelDefault = token.getLabel();
        mImageDefault = token.getImage();

        // Get references to widgets.
        mIssuer = (EditText) findViewById(R.id.issuer);
        mLabel = (EditText) findViewById(R.id.label);
        mImage = (ImageButton) findViewById(R.id.image);
        mRestore = (Button) findViewById(R.id.restore);
        mSave = (Button) findViewById(R.id.save);

        // Setup text changed listeners.
        mIssuer.addTextChangedListener(this);
        mLabel.addTextChangedListener(this);

        // Setup click callbacks.
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.restore).setOnClickListener(this);
        mImage.setOnClickListener(this);

        // Setup initial state.
        showImage(mImageCurrent);
        mLabel.setText(mLabelCurrent);
        mIssuer.setText(mIssuerCurrent);
        mIssuer.setSelection(mIssuer.getText().length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
            showImage(data.getData());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String label = mLabel.getText().toString();
        String issuer = mIssuer.getText().toString();
        mSave.setEnabled(!label.equals(mLabelCurrent) || !issuer.equals(mIssuerCurrent) || !imageIs(mImageCurrent));
        mRestore.setEnabled(!label.equals(mLabelDefault) || !issuer.equals(mIssuerDefault) || !imageIs(mImageDefault));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 0);
                break;

            case R.id.restore:
                mLabel.setText(mLabelDefault);
                mIssuer.setText(mIssuerDefault);
                mIssuer.setSelection(mIssuer.getText().length());
                showImage(mImageDefault);
                break;

            case R.id.save:
                TokenPersistence tp = new TokenPersistence(this);
                Token token = tp.get(getPosition());
                token.setIssuer(mIssuer.getText().toString());
                token.setLabel(mLabel.getText().toString());
                token.setImage(mImageDisplay.toString());
                tp.save(token);

            case R.id.cancel:
                finish();
                break;
        }
    }
}
