package com.forgerock.authenticator.mechanisms;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.forgerock.authenticator.mechanisms.TOTP.Token;

public abstract class MechanismLayout extends FrameLayout implements View.OnClickListener, Runnable {
    public MechanismLayout(Context context) {
        super(context);
    }

    public MechanismLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MechanismLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
