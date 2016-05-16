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

package com.forgerock.authenticator.identity;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forgerock.authenticator.MechanismActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.baseactivities.BaseIdentityActivity;
import com.forgerock.authenticator.delete.DeleteIdentityActivity;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual entry which displays information about a given Identity.
 */
public class IdentityLayout extends RelativeLayout {

    private ActionMode actionMode;

    //Constructors used automatically by Android. Will never be called directly.
    public IdentityLayout(Context context) {
        super(context);
    }
    public IdentityLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public IdentityLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Used to get access to the ActionMode for testing.
     * @return The ActionMode belonging to this layout.
     */
    @VisibleForTesting
    public ActionMode getActionMode() {
        return actionMode;
    }

    /**
     * Set the Identity that this Layout displays.
     * @param identity The Identity to display.
     */
    void bind(final Identity identity) {
        TextView issuerView = (TextView) findViewById(R.id.issuer);
        TextView labelView = (TextView) findViewById(R.id.label);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        issuerView.setText(identity.getIssuer());
        labelView.setText(identity.getAccountName());

        Picasso.with(getContext())
                .load(identity.getImageURL())
                .placeholder(R.drawable.forgerock_placeholder)
                .into(imageView);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseIdentityActivity.start(getContext(), MechanismActivity.class, identity);
            }
        });

        final ContextualActionBar actionBar = new ContextualActionBar(getContext(), identity);

        setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                if (actionMode != null) {
                    return false;
                }
                actionMode = startActionMode(actionBar);
                view.setSelected(true);
                return true;
            }
        });

        // There should currently only be at most two mechanisms associated with the Identity
        List<MechanismIcon> icons = new ArrayList<>();
        icons.add((MechanismIcon) findViewById(R.id.iconA));
        icons.add((MechanismIcon) findViewById(R.id.iconB));

        for (int i = 0; i < icons.size(); i++) {
            icons.get(i).setVisibility(GONE);
        }

        for (int i = 0; i < identity.getMechanisms().size() && i < icons.size(); i++) {
            icons.get(i).setVisibility(VISIBLE);
            icons.get(i).setMechanism(identity.getMechanisms().get(i));
        }
    }

    /**
     * Action Bar which is displayed when an Identity is long pressed.
     */
    private class ContextualActionBar implements ActionMode.Callback {

        private final Context context;
        private final Identity identity;

        public ContextualActionBar(Context context, Identity identity) {
            this.context = context;
            this.identity = identity;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.identity, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                BaseIdentityActivity.start(context, DeleteIdentityActivity.class, identity);
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }
}
