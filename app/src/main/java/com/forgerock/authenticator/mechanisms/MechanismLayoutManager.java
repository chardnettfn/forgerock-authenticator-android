package com.forgerock.authenticator.mechanisms;

import android.content.Context;
import android.view.View;

public abstract class MechanismLayoutManager {
    public abstract void bind(final Context context, Mechanism mechanism, View view);
    public abstract int getLayoutType();
}
