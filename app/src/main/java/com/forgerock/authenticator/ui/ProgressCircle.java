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

package com.forgerock.authenticator.ui;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.forgerock.authenticator.R;

public class ProgressCircle extends View {
    private Paint   mPaint;
    private RectF   mRectF;
    private Rect    mRect;
    private int     mProgress;
    private int     mMax;
    private boolean mHollow;
    private float   mPadding;
    private float   mStrokeWidth;
    private int normalColor;
    private int warningColor;

    public ProgressCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    public ProgressCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public ProgressCircle(Context context) {
        super(context);
        setup(context, null);
    }

    private void setup(Context context, AttributeSet attrs) {
        normalColor = getContext().getResources().getColor(R.color.normal_timer_color);
        warningColor = getContext().getResources().getColor(R.color.warning_timer_color);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm);

        mRectF = new RectF();
        mRect = new Rect();

        mPaint = new Paint();
        mPaint.setARGB(0x99, 0x33, 0x33, 0x33);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        if (attrs != null) {
            Theme t = context.getTheme();
            TypedArray a = t.obtainStyledAttributes(attrs, R.styleable.ProgressCircle, 0, 0);

            try {
                setMax(a.getInteger(R.styleable.ProgressCircle_max, 100));
                setHollow(a.getBoolean(R.styleable.ProgressCircle_hollow, false));
            } finally {
                a.recycle();
            }
        }
    }

    public void setMax(int max) {
        this.mMax = max;
    }

    public int getMax() {
        return mMax;
    }

    public void setHollow(boolean hollow) {
        mHollow = hollow;
        mPaint.setStyle(hollow ? Style.STROKE : Style.FILL);
        mPaint.setStrokeWidth(hollow ? mStrokeWidth : 0);
    }

    public boolean getHollow() {
        return mHollow;
    }

    public void setProgress(int progress) {
        mProgress = progress;

        int percent = mProgress * 100 / getMax();
        if (percent < 90 || mProgress == 0) {
            mPaint.setColor(normalColor);
        } else {
            mPaint.setColor(warningColor);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mRect);

        mRect.left += getPaddingLeft() + mPadding;
        mRect.top += getPaddingTop() + mPadding;
        mRect.right -= getPaddingRight() + mPadding;
        mRect.bottom -= getPaddingBottom() + mPadding;
        mRectF.set(mRect);

        canvas.drawArc(mRectF, -90, mProgress * 360 / getMax(), !mHollow, mPaint);
    }
}
