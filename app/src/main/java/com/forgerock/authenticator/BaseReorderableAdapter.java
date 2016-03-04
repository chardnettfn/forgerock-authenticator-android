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

package com.forgerock.authenticator;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseReorderableAdapter extends BaseAdapter {
    private class Reference<T> {
        public Reference(T t) {
            reference = t;
        }

        T reference;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = createView(parent, type);

            convertView.setOnDragListener(new OnDragListener() {
                @Override
                public boolean onDrag(View dstView, DragEvent event) {
                    Reference<View> ref = (Reference<View>) event.getLocalState();
                    final View srcView = ref.reference;

                    switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        srcView.setVisibility(View.VISIBLE);
                        dstView.setVisibility(View.INVISIBLE);

                        move(((Integer) srcView.getTag(R.id.reorder_key)).intValue(),
                             ((Integer) dstView.getTag(R.id.reorder_key)).intValue());
                        ref.reference = dstView;
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        srcView.post(new Runnable() {
                            @Override
                            public void run() {
                                srcView.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    }

                    return true;
                }
            });

            convertView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    // Force a reset of any states
                    notifyDataSetChanged();

                    // Start the drag on the main loop to allow
                    // the above state reset to settle.
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            ClipData data = ClipData.newPlainText("", "");
                            DragShadowBuilder sb = new View.DragShadowBuilder(view);
                            view.startDrag(data, sb, new Reference<View>(view), 0);
                        }
                    });

                    return true;
                }
            });
        }

        convertView.setTag(R.id.reorder_key, Integer.valueOf(position));
        bindView(convertView, position);
        return convertView;
    }

    protected abstract void move(int fromPosition, int toPosition);

    protected abstract void bindView(View view, int position);

    protected abstract View createView(ViewGroup parent, int type);
}
