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

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.forgerock.authenticator.NotificationActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseMechanismActivity;
import com.forgerock.authenticator.delete.DeleteMechanismActivity;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.forgerock.authenticator.mechanisms.base.MechanismLayout;

/**
 * Handles the display of a Push mechanism in a list.
 */
public class PushLayout extends MechanismLayout<Push> {
    private Push push;

    public PushLayout(Context context) {
        super(context);
    }

    public PushLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PushLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onContextualActionBarItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            BaseMechanismActivity.start(getContext(), DeleteMechanismActivity.class, push);
        }
    }

    @Override
    protected int getContextMenu() {
        return R.menu.push;
    }

    @Override
    public void bind(final Push mechanism) {
        this.push = mechanism;
        MechanismIcon icon = (MechanismIcon) findViewById(R.id.icon);
        icon.setMechanism(mechanism);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseMechanismActivity.start(getContext(), NotificationActivity.class, mechanism);
            }
        });
    }
}
