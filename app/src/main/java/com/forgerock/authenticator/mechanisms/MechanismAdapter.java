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
 *
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.mechanisms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.DatabaseListener;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.mechanisms.TOTP.TokenLayoutManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;

public class MechanismAdapter extends BaseAdapter implements DatabaseListener {
    private final IdentityDatabase identityDatabase;
    private final LayoutInflater mLayoutInflater;
    private final Identity owner;
    private List<Mechanism> mechanismList;
    private Map<Integer, MechanismLayoutManager> layoutTypeMap;

    public MechanismAdapter(Context context, Identity owner) {
        identityDatabase = getIdentityDatabase(context);
        identityDatabase.addListener(this);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.owner = owner;
        mechanismList = identityDatabase.getMechanisms(owner);
        layoutTypeMap = new HashMap<>();
        layoutTypeMap.put(0, new TokenLayoutManager());
    }

    public IdentityDatabase getIdentityDatabase(Context context) {
        return RoboGuice.getInjector(context).getInstance(IdentityDatabase.class);
    }

    @Override
    public int getCount() {
        return mechanismList.size(); //TODO: Don't refetch list unless necessary
    }

    @Override
    public Mechanism getItem(int position) {
        return mechanismList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getRowId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = mLayoutInflater.inflate(layoutTypeMap.get(type).getLayoutType(), parent, false);
        }

        convertView.setTag(R.id.reorder_key, Integer.valueOf(position)); // TODO: Is this needed?

        Mechanism mechanism = getItem(position);
        mechanism.getLayoutManager().bind(convertView.getContext(), mechanism, convertView);
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return layoutTypeMap.size();
    }

    @Override
    public int getItemViewType(int position) {
        for (int key : layoutTypeMap.keySet()) { //TODO: more elegant solution
            if (layoutTypeMap.get(key).getLayoutType() == getItem(position).getLayoutManager().getLayoutType()) {
                return key;
            }
        }
        return -1;
    }

    @Override
    public void onUpdate() {
        mechanismList = identityDatabase.getMechanisms(owner);
        notifyDataSetChanged();
    }
}
