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

package com.forgerock.authenticator.identity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.storage.DatabaseListener;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.NotStoredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import roboguice.RoboGuice;

/**
 * Class for displaying the complete list of Identities,
 */
public class IdentityAdapter extends BaseAdapter implements DatabaseListener {
    private final IdentityDatabase identityDatabase;
    private final LayoutInflater mLayoutInflater;
    private List<Identity> identityList;
    private Logger logger = LoggerFactory.getLogger(IdentityAdapter.class);


    /**
     * Creates the adapter, and sets up the database connection.
     * @param context The context the adapter is being created in.
     */
    public IdentityAdapter(Context context) {
        identityDatabase = getIdentityDatabase(context);
        identityDatabase.addListener(this);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        identityList = identityDatabase.getIdentities();
    }

    /**
     * Gets the database used for storing the data. Exposed for testing.
     * @param context The current context.
     * @return The shared database connection.
     */
    public IdentityDatabase getIdentityDatabase(Context context) {
        return RoboGuice.getInjector(context).getInstance(IdentityDatabase.class);
    }

    @Override
    public int getCount() {
        return identityList.size();
    }

    @Override
    public Identity getItem(int position) {
        return identityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return getItem(position).getId();
        } catch (NotStoredException e) {
            // This should never happen, as the mechanismList is populated directly from the database.
            logger.error("Identity loaded from database did not contain row id.", e);
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.identitycell, parent, false);
        }

        Identity identity = getItem(position);
        ((IdentityLayout) convertView).bind(identity);
        return convertView;
    }

    @Override
    public void onUpdate() {
        identityList = identityDatabase.getIdentities();
        notifyDataSetChanged();
    }
}
