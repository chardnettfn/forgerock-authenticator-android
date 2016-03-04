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
 * Portions Copyright 2014 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.add;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ScanWindowFrameLayout extends FrameLayout {
    public ScanWindowFrameLayout(Context context) {
        super(context);
    }

    public ScanWindowFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScanWindowFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Ensure that this view is always a square.
        if (widthMeasureSpec > heightMeasureSpec)
            widthMeasureSpec = heightMeasureSpec;
        else
            heightMeasureSpec = widthMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
