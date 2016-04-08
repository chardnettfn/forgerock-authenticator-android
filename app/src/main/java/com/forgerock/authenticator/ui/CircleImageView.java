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

package com.forgerock.authenticator.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class for displaying an image inside of a circle.
 */
public class CircleImageView extends ImageView {
    private Paint white;

    /**
     * Create a circle that an image is displayed within.
     * @param context The context that the view is created from.
     */
    public CircleImageView(Context context) {
        super(context);
        init();
    }

    /**
     * Create a circle that an image is displayed within.
     * @param context The context that the view is created from.
     * @param attrs The list of attributes.
     */
    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Create a circle that an image is displayed within.
     * @param context The context that the view is created from.
     * @param attrs The list of attributes.
     * @param defStyleAttr The resource containing default style attributes.
     */
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        white = new Paint();
        white.setARGB(255, 255, 255, 255);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2, white);

        // Used to determine the actual size of the View
        measure(getWidth(), getHeight());
        double smallestDimension = getMeasuredWidth() < getMeasuredHeight() ? getMeasuredWidth() : getMeasuredHeight();

        double newSize = smallestDimension / Math.sqrt(2);
        int sizeDifference = (int) ((smallestDimension - newSize) / 2);

        Rect bounds = drawable.getBounds();
        bounds.inset(sizeDifference, sizeDifference);
        drawable.setBounds(bounds);
        setImageDrawable(drawable);

        super.onDraw(canvas);

    }
}
