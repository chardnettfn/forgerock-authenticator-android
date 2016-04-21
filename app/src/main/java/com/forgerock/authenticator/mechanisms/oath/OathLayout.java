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
 */

package com.forgerock.authenticator.mechanisms.oath;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.forgerock.authenticator.baseactivities.BaseMechanismActivity;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.forgerock.authenticator.ui.ProgressCircle;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.delete.DeleteMechanismActivity;
import com.forgerock.authenticator.mechanisms.base.MechanismLayout;

/**
 * Handles the display of a Token in a list.
 * Some common features of this may be able to be broken out.
 */
public class OathLayout extends FrameLayout implements MechanismLayout<Oath> {
    private ProgressCircle mProgressOuter;
    private TextView mCode;
    private ImageView mMenu;
    private PopupMenu mPopupMenu;

    private TokenCode mCodes;
    private String mPlaceholder;
    private ImageView refresh;

    /**
     * Creates this layout using the provided context.
     * @param context The context this layout exists within.
     */
    public OathLayout(Context context) {
        super(context);
    }

    public OathLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OathLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mProgressOuter = (ProgressCircle) findViewById(R.id.progressOuter);
        mCode = (TextView) findViewById(R.id.code);
        mMenu = (ImageView) findViewById(R.id.menu);
        refresh = (ImageView) findViewById(R.id.refresh);

        mPopupMenu = new PopupMenu(getContext(), mMenu);
        mMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
    }

    @Override
    public void bind(final Oath oath) {

        final Context context = this.getContext();
        mCodes = null;
        // Cancel all active animations.
        setEnabled(true);
        mProgressOuter.clearAnimation();

        MechanismIcon icon = (MechanismIcon) findViewById(R.id.icon);
        icon.setMechanism(oath);

        // Setup menu.
        mPopupMenu.getMenu().clear();
        mPopupMenu.getMenuInflater().inflate(R.menu.token, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    BaseMechanismActivity.start(context, DeleteMechanismActivity.class, oath);
                }
                return true;
            }
        });

        switch (oath.getType()) {
            case HOTP:
                setupHOTP(oath);
                break;
            case TOTP:
                setupTOTP(oath);
                break;
        }
    }

    private void setupTOTP(final Oath oath) {
        mProgressOuter.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.GONE);
        mCodes = oath.generateNextCode();

        Runnable totpRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the current data
                if (!mCodes.isValid()) {
                    mCodes = oath.generateNextCode();
                }

                String code = mCodes.getCurrentCode();

                // Update the fields
                mCode.setText(code);
                mProgressOuter.setProgress(mCodes.getCurrentProgress());
                postDelayed(this, 100);
            }
        };
        post(totpRunnable);
    }

    private void setupHOTP(final Oath oath) {

        mProgressOuter.setVisibility(View.GONE);
        refresh.setVisibility(View.VISIBLE);

        // Get the code placeholder.
        StringBuilder placeholderBuilder = new StringBuilder();
        for (int i = 0; i < oath.getDigits(); i++) {
            placeholderBuilder.append('●');
            if (i == oath.getDigits() / 2 - 1) {
                placeholderBuilder.append(' ');
            }
        }
        mPlaceholder = new String(placeholderBuilder);

        // Set the labels.
        mCode.setText(mPlaceholder);

        // Set onClick behaviour
        final Context context = getContext();
        final View view = this;

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the token.
                TokenCode code = oath.generateNextCode();

                mCode.setText(code.getCurrentCode());

                setEnabled(false);
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 5000);
            }
        });
    }
}