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

package com.forgerock.authenticator.mechanisms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.DatabaseListener;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.NotStoredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

/**
 * Class for displaying a list of mechanisms belonging to a particular identity.
 * Keeps itself in sync the database by listening to it for updates.
 */
public class MechanismAdapter extends BaseAdapter implements DatabaseListener {
    private final IdentityDatabase identityDatabase;
    private final LayoutInflater mLayoutInflater;
    private final Identity owner;
    private List<Mechanism> mechanismList;
    private List<Integer> layoutTypes;
    private Logger logger = LoggerFactory.getLogger(MechanismAdapter.class);

    /**
     * Creates an adapter which contains all of the mechanisms related to a particular identity.
     * @param context
     * @param owner
     */
    public MechanismAdapter(Context context, Identity owner) {
        identityDatabase = getIdentityDatabase(context);
        identityDatabase.addListener(this);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.owner = owner;
        mechanismList = identityDatabase.getMechanisms(owner);
        layoutTypes = new ArrayList<>();
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            layoutTypes.add(info.getLayoutType());
        }
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
        return mechanismList.size();
    }

    @Override
    public Mechanism getItem(int position) {
        return mechanismList.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return getItem(position).getRowId();
        } catch (NotStoredException e) {
            // This should never happen, as the mechanismList is populated directly from the database.
            logger.error("Mechanism loaded from database did not contain row id.", e);
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int typeIndex = getItemViewType(position);
            convertView = mLayoutInflater.inflate(layoutTypes.get(typeIndex), parent, false);
        }

        Mechanism mechanism = getItem(position);
        mechanism.getInfo().bind(convertView, mechanism);
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return layoutTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return layoutTypes.indexOf(getItem(position).getInfo().getLayoutType());
    }

    @Override
    public void onUpdate() {
        mechanismList = identityDatabase.getMechanisms(owner);
        notifyDataSetChanged();
    }
}
