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
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.base.MechanismInfo;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

/**
 * Class for displaying a list of mechanisms belonging to a particular identity.
 * Keeps itself in sync the database by listening to it for updates.
 */
public class MechanismAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private Identity owner;
    private List<Mechanism> mechanismList;
    private List<Integer> layoutTypes;

    /**
     * Creates an adapter which contains all of the mechanisms related to a particular identity.
     * @param context The context the adapter is being used in.
     * @param owner The identity which is having its mechanism loaded.
     */
    public MechanismAdapter(Context context, final Identity owner) {
        this.owner = owner;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mechanismList = owner.getMechanisms();

        layoutTypes = new ArrayList<>();
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            layoutTypes.add(info.getLayoutType());
        }
    }

    @Override
    public int getCount() {
        return mechanismList.size();
    }

    @Override
    public Mechanism getItem(int position) {
        return mechanismList.get(position);
    }

    public long getItemId(int position) {
        return 0;
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
    public void notifyDataSetChanged() {
        mechanismList = owner.getMechanisms();
        super.notifyDataSetChanged();
    }
}
