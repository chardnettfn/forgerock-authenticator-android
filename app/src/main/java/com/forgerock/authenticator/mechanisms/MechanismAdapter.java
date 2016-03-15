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

import com.forgerock.authenticator.BaseReorderableAdapter;
import com.forgerock.authenticator.ProgressCircle;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.identity.IdentityDatabase;
import com.forgerock.authenticator.mechanisms.TOTP.TokenLayoutManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MechanismAdapter extends BaseReorderableAdapter {
    private final IdentityDatabase identityDatabase;
    private final LayoutInflater mLayoutInflater;
    private final Identity owner;
    private List<Mechanism> mechanismList;
    private Map<Integer, MechanismLayoutManager> layoutTypeMap;

    public MechanismAdapter(Context ctx, Identity owner) {
        identityDatabase = new IdentityDatabase(ctx);
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.owner = owner;
        mechanismList = new IdentityDatabase(ctx).getMechanisms(owner);
        layoutTypeMap = new HashMap<>();
        layoutTypeMap.put(0, new TokenLayoutManager());
    }

    @Override
    public int getCount() {
        mechanismList = identityDatabase.getMechanisms(owner);

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
    protected void move(int fromPosition, int toPosition) {
        //Do nothing
    }

    @Override
    protected void bindView(View view, final int position) {
        Mechanism mechanism = getItem(position);
        mechanism.getLayoutManager().bind(view.getContext(), mechanism, view);
    }

    @Override
    protected View createView(ViewGroup parent, int type) {
        return mLayoutInflater.inflate(layoutTypeMap.get(type).getLayoutType(), parent, false);
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
}
