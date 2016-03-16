package com.forgerock.authenticator.mechanisms.TOTP;

import android.view.View;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.IMechanismFactory;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismInfo;

public class TokenInfo implements MechanismInfo {
    @Override
    public void bind(View view, Mechanism mechanism) {
        TokenLayout tl = (TokenLayout) view;
        tl.bind((Token) mechanism);
    }

    @Override
    public int getLayoutType() {
        return R.layout.token;
    }

    @Override
    public String getMechanismString() {
        return "OTP";
    }

    @Override
    public IMechanismFactory getFactory() {
        return new TokenFactory();
    }

    @Override
    public boolean matchesURI(String uri) {
        return true; //TODO: Update when there are more URI types
    }
}
