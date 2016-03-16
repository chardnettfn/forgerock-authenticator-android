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
 * Copyright 2015 ForgeRock AS.
 */

package com.forgerock.authenticator.mechanisms.TOTP;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.forgerock.authenticator.ProgressCircle;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.edit.DeleteActivity;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismLayout;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.squareup.picasso.Picasso;

/**
 * Handles the display of a Token in a list.
 */
class TokenLayout extends FrameLayout implements View.OnClickListener, Runnable, MechanismLayout<Token> {
    private ProgressCircle mProgressInner;
    private ProgressCircle mProgressOuter;
    private ImageView mImage;
    private TextView mCode;
    private TextView mIssuer;
    private TextView mLabel;
    private ImageView mMenu;
    private PopupMenu mPopupMenu;

    private TokenCode mCodes;
    private Token.TokenType mType;
    private String mPlaceholder;
    private long mStartTime;

    public TokenLayout(Context context) {
        super(context);
    }

    public TokenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TokenLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mProgressInner = (ProgressCircle) findViewById(R.id.progressInner);
        mProgressOuter = (ProgressCircle) findViewById(R.id.progressOuter);
        mImage = (ImageView) findViewById(R.id.image);
        mCode = (TextView) findViewById(R.id.code);
        mIssuer = (TextView) findViewById(R.id.issuer);
        mLabel = (TextView) findViewById(R.id.label);
        mMenu = (ImageView) findViewById(R.id.menu);

        mPopupMenu = new PopupMenu(getContext(), mMenu);
        mMenu.setOnClickListener(this);
    }

    @Override
    public void bind(final Token token) {
        final Context context = this.getContext();
        bindData(token, R.menu.token, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i;

                switch (item.getItemId()) {

                    case R.id.action_delete:
                        i = new Intent(context, DeleteActivity.class);
                        i.putExtra(DeleteActivity.ROW_ID, token.getRowId());
                        context.startActivity(i);
                        break;
                }

                return true;
            }
        });

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the token.
                TokenCode codes = token.generateCodes();
                new IdentityDatabase(v.getContext()).updateMechanism(token);

                ((TokenLayout) v).start(token.getType(), codes, true);
            }
        });
    }

    private void bindData(Token token, int menu, PopupMenu.OnMenuItemClickListener micl) {
        mCodes = null;

        // Setup menu.
        mPopupMenu.getMenu().clear();
        mPopupMenu.getMenuInflater().inflate(menu, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(micl);

        // Cancel all active animations.
        setEnabled(true);
        removeCallbacks(this);
        mImage.clearAnimation();
        mProgressInner.clearAnimation();
        mProgressOuter.clearAnimation();
        mProgressInner.setVisibility(View.GONE);
        mProgressOuter.setVisibility(View.GONE);

        // Get the code placeholder.
        char[] placeholder = new char[token.getDigits()];
        for (int i = 0; i < placeholder.length; i++)
            placeholder[i] = '-';
        mPlaceholder = new String(placeholder);

        // Show the image.
        Picasso.with(getContext())
                .load(token.getOwner().getImage())
                .placeholder(R.drawable.forgerock_logo)
                .into(mImage);

        // Set the labels.
        mLabel.setText(token.getOwner().getLabel());
        mIssuer.setText(token.getOwner().getIssuer());
        mCode.setText(mPlaceholder);
        if (mIssuer.getText().length() == 0) {
            mIssuer.setText(token.getOwner().getLabel());
            mLabel.setVisibility(View.GONE);
        } else {
            mLabel.setVisibility(View.VISIBLE);
        }
    }

    private void animate(View view, int anim, boolean animate) {
        Animation a = AnimationUtils.loadAnimation(view.getContext(), anim);
        if (!animate)
            a.setDuration(0);
        view.startAnimation(a);
    }

    private void start(Token.TokenType type, TokenCode codes, boolean animate) {
        mCodes = codes;
        mType = type;

        // Start animations.
        mProgressInner.setVisibility(View.VISIBLE);
        animate(mProgressInner, R.anim.fadein, animate);
        animate(mImage, R.anim.token_image_fadeout, animate);

        // Handle type-specific UI.
        switch (type) {
            case HOTP:
                setEnabled(false);
                break;
            case TOTP:
                mProgressOuter.setVisibility(View.VISIBLE);
                animate(mProgressOuter, R.anim.fadein, animate);
                break;
        }

        mStartTime = System.currentTimeMillis();
        post(this);
    }

    @Override
    public void onClick(View v) {
        mPopupMenu.show();
    }

    @Override
    public void run() {
        // Get the current data
        String code = mCodes == null ? null : mCodes.getCurrentCode();
        if (code != null) {
            // Determine whether to enable/disable the view.
            if (!isEnabled())
                setEnabled(System.currentTimeMillis() - mStartTime > 5000);

            // Update the fields
            mCode.setText(code);
            mProgressInner.setProgress(mCodes.getCurrentProgress());
            if (mType != Token.TokenType.HOTP)
                mProgressOuter.setProgress(mCodes.getTotalProgress());

            postDelayed(this, 100);
            return;
        }

        mCode.setText(mPlaceholder);
        mProgressInner.setVisibility(View.GONE);
        mProgressOuter.setVisibility(View.GONE);
        animate(mImage, R.anim.token_image_fadein, true);
    }
}