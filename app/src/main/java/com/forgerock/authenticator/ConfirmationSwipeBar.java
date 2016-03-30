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

package com.forgerock.authenticator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * UI element which is a bar which can be swiped from left to right. When the right side is reached,
 * and the user releases their finger, the action is complete. A listener can be set which will
 * receive an event when this happens. The bar can be reset by calling unselect().
 */
public class ConfirmationSwipeBar extends SeekBar {
    private ConfirmationSwipeBarListener listener;
    private boolean isTrackingTouch;
    private Drawable thumb;
    private boolean selectionComplete = false;

    public ConfirmationSwipeBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public ConfirmationSwipeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ConfirmationSwipeBar(Context context) {
        super(context);
        setup();
    }

    private void setup() {
        setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
                if (seekBar.getProgress() == 100) {
                    listener.onConfirm();
                    completeSelection();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }
        });
    }

    private void completeSelection() {
        selectionComplete = true;
    }

    @Override
    public void setThumb(Drawable thumb) {
        this.thumb = thumb;
        super.setThumb(thumb);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!selectionComplete && (!(event.getAction() == MotionEvent.ACTION_DOWN)
                || thumb.copyBounds().contains((int) event.getX(), (int) event.getY()))) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isTrackingTouch && !selectionComplete) {
            setProgress((getProgress()) / 2);
        }

        super.onDraw(canvas);
    }

    /**
     * Reset the bar to its default state (thumb at far left)
     */
    public void unselect() {
        setProgress(0);
        selectionComplete = false;
    }

    /**
     * Sets up the listener which can receive the onConfirm event.
     * @param listener The listener to use. Only one listener can be set.
     */
    public void setListener(ConfirmationSwipeBarListener listener) {
        this.listener = listener;
    }

    /**
     * Listener class which waits for the onConfirm() function to be called.
     */
    public static abstract class ConfirmationSwipeBarListener {
        /**
         * Called when the swipe bar is wiped all the way to the right.
         */
        public abstract void onConfirm();
    }


}
