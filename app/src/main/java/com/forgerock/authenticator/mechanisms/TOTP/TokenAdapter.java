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

package com.forgerock.authenticator.mechanisms.TOTP;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.forgerock.authenticator.BaseReorderableAdapter;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.edit.DeleteActivity;
import com.forgerock.authenticator.edit.EditActivity;

import java.util.HashMap;
import java.util.Map;

public class TokenAdapter extends BaseReorderableAdapter {
    private final TokenPersistence mTokenPersistence;
    private final LayoutInflater mLayoutInflater;
    private final Map<String, TokenCode> mTokenCodes;

    public TokenAdapter(Context ctx) {
        mTokenPersistence = new TokenPersistence(ctx);
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTokenCodes = new HashMap<String, TokenCode>();
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mTokenCodes.clear();
            }

            @Override
            public void onInvalidated() {
                mTokenCodes.clear();
            }
        });
    }

    @Override
    public int getCount() {
        return mTokenPersistence.length();
    }

    @Override
    public Token getItem(int position) {
        return mTokenPersistence.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void move(int fromPosition, int toPosition) {
        mTokenPersistence.move(fromPosition, toPosition);
        notifyDataSetChanged();
    }

    @Override
    protected void bindView(View view, final int position) {
        final Context ctx = view.getContext();
        TokenLayout tl = (TokenLayout) view;
        Token token = getItem(position);

        tl.bind(token, R.menu.token, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i;

                switch (item.getItemId()) {
                    case R.id.action_edit:
                        i = new Intent(ctx, EditActivity.class);
                        i.putExtra(EditActivity.EXTRA_POSITION, position);
                        ctx.startActivity(i);
                        break;

                    case R.id.action_delete:
                        i = new Intent(ctx, DeleteActivity.class);
                        i.putExtra(DeleteActivity.EXTRA_POSITION, position);
                        ctx.startActivity(i);
                        break;
                }

                return true;
            }
        });

        tl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TokenPersistence tp = new TokenPersistence(ctx);

                // Increment the token.
                Token token = tp.get(position);
                TokenCode codes = token.generateCodes();
                tp.save(token);

                mTokenCodes.put(token.getID(), codes);
                ((TokenLayout) v).start(token.getType(), codes, true);
            }
        });

        TokenCode tc = mTokenCodes.get(token.getID());
        if (tc != null && tc.getCurrentCode() != null)
            tl.start(token.getType(), tc, false);
    }

    @Override
    protected View createView(ViewGroup parent, int type) {
        return mLayoutInflater.inflate(R.layout.token, parent, false);
    }
}
