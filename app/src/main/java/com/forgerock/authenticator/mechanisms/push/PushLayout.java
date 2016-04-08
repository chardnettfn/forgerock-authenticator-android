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
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.forgerock.authenticator.NotificationActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.delete.DeleteMechanismActivity;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.forgerock.authenticator.mechanisms.base.MechanismLayout;

/**
 * Handles the display of a Push mechanism in a list.
 */
public class PushLayout extends FrameLayout implements MechanismLayout<Push> {

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
    public void bind(final Push mechanism) {
        MechanismIcon icon = (MechanismIcon) findViewById(R.id.icon);
        icon.setMechanism(mechanism);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                Intent intent = new Intent(context, NotificationActivity.class);
                intent.putExtra(NotificationActivity.IDENTITY_REFERENCE, mechanism.getOwner().getOpaqueReference());
                context.startActivity(intent);
            }
        });

        ImageView menu = (ImageView) findViewById(R.id.menu);
        final PopupMenu popupMenu = new PopupMenu(getContext(), menu);
        menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        // Setup menu.
        popupMenu.getMenu().clear();
        popupMenu.getMenuInflater().inflate(R.menu.token, popupMenu.getMenu());
        final Context context = getContext();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i;

                switch (item.getItemId()) {

                    case R.id.action_delete:
                        i = new Intent(context, DeleteMechanismActivity.class);
                        i.putExtra(DeleteMechanismActivity.MECHANISM_REFERENCE, mechanism.getOpaqueReference());
                        context.startActivity(i);
                        break;
                }

                return true;
            }
        });
    }
}
