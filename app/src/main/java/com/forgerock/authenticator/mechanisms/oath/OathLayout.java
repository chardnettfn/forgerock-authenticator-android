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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.baseactivities.BaseMechanismActivity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.forgerock.authenticator.ui.ProgressCircle;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.delete.DeleteMechanismActivity;
import com.forgerock.authenticator.mechanisms.base.MechanismLayout;

/**
 * Handles the display of a Token in a list.
 * Some common features of this may be able to be broken out.
 */
public class OathLayout extends MechanismLayout<Oath> {
    private ProgressCircle mProgressOuter;
    private TextView mCode;

    private TokenCode mCodes;
    private String mPlaceholder;
    private ImageButton refresh;
    private Oath oath;
    private String code;

    private static final int HOTP_COOLDOWN = 5000;
    private static final int TOTP_TICK = 100;

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
        refresh = (ImageButton) findViewById(R.id.refresh);
    }

    @Override
    public void bind(final Oath oath) {
        this.oath = oath;

        mCodes = null;
        // Cancel all active animations.
        setEnabled(true);
        mProgressOuter.clearAnimation();

        MechanismIcon icon = (MechanismIcon) findViewById(R.id.icon);
        icon.setMechanism(oath);

        switch (oath.getType()) {
            case HOTP:
                setupHOTP(oath);
                break;
            case TOTP:
                setupTOTP(oath);
                break;
        }
    }

    @Override
    public void onContextualActionBarItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                BaseMechanismActivity.start(getContext(), DeleteMechanismActivity.class, oath);
            case R.id.action_copy:
                if (code != null) {
                    ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = ClipData.newPlainText(getContext().getString(R.string.copy_oath_label), code);
                    manager.setPrimaryClip(data);
                }
        }
    }

    @Override
    protected void onPrepareContextualActionBar(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_copy).setEnabled(code != null);
    }

    @Override
    protected int getContextMenu() {
        return R.menu.oath;
    }

    private void setupTOTP(final Oath oath) {
        mProgressOuter.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.GONE);
        mCodes = oath.generateNextCode();

        Runnable totpRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mCodes.isValid()) {
                    mCodes = oath.generateNextCode();
                }

                code = mCodes.getCurrentCode();

                setDisplayCode(code);

                mProgressOuter.setProgress(mCodes.getCurrentProgress());
                postDelayed(this, TOTP_TICK);
            }
        };
        post(totpRunnable);
    }

    private void setupHOTP(final Oath oath) {

        mProgressOuter.setVisibility(View.GONE);
        refresh.setVisibility(View.VISIBLE);

        StringBuilder placeholderBuilder = new StringBuilder();
        for (int i = 0; i < oath.getDigits(); i++) {
            placeholderBuilder.append('•');
            if (i == oath.getDigits() / 2 - 1) {
                placeholderBuilder.append(' ');
            }
        }
        mPlaceholder = new String(placeholderBuilder);

        mCode.setText(mPlaceholder);

        final View view = this;

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the code.
                code = oath.generateNextCode().getCurrentCode();
                setDisplayCode(code);
                refresh.setEnabled(false);

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh.setEnabled(true);
                    }
                }, HOTP_COOLDOWN);
            }
        });
    }

    private void setDisplayCode(String code) {
        String formattedCode = code.substring(0, code.length() / 2) + " " +
                code.substring(code.length() / 2, code.length());

        mCode.setText(formattedCode);
    }

}