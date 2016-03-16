package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.mechanisms.TOTP.TokenInfo;

/**
 * Provides a complete list of all possible mechanisms
 */
public class MechanismList {

    /**
     * Should contain an instance of all possible MechanismInfos.
     * @return The list of MechanismInfos.
     */
    public static MechanismInfo[] getAllMechanisms() {
        return new MechanismInfo[] {
                new TokenInfo()
        };
    }
}
