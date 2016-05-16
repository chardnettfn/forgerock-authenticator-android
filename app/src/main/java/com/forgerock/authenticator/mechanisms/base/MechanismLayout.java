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

package com.forgerock.authenticator.mechanisms.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.forgerock.authenticator.R;

/**
 * Base interface for Mechanism layouts, which handles all aspects relating to display.
 */
public abstract class MechanismLayout<T extends Mechanism> extends FrameLayout {
    private boolean isCABActive = false;

    public MechanismLayout(Context context) {
        super(context);
        setup();
    }

    public MechanismLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public MechanismLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        final ContextualActionBar actionBar = new ContextualActionBar();

        setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                if (isCABActive) {
                    return false;
                }
                isCABActive = true;
                startActionMode(actionBar);
                view.setSelected(true);
                return true;
            }
        });
    }

    /**
     * Get the resource for the menu displayed for this Mechanism.
     * @return The resource id.
     */
    protected abstract int getContextMenu();

    /**
     * Hook for enabling or disabling menu elements
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     */
    protected void onPrepareContextualActionBar(ActionMode mode, Menu menu) {
        // Do nothing by default
    }

    /**
     * Hook for reacting to a contextual action bar click.
     * @param mode The current ActionMode
     * @param item The item that was clicked
     */
    public abstract void onContextualActionBarItemClicked(ActionMode mode, MenuItem item);

    /**
     * Sets up this layout to represent the provided mechanism.
     * @param mechanism The mechanism to display.
     */
    public abstract void bind(T mechanism);

    /**
     * Action Bar which is displayed when the Mechanism is long pressed.
     */
    private class ContextualActionBar implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(getContextMenu(), menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            onPrepareContextualActionBar(mode, menu);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            onContextualActionBarItemClicked(mode, item);
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isCABActive = false;
        }
    }
}
