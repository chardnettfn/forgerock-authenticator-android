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

package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.mechanisms.base.MechanismInfo;
import com.forgerock.authenticator.mechanisms.oath.OathInfo;
import com.forgerock.authenticator.mechanisms.push.PushInfo;

/**
 * Provides a complete list of all possible mechanisms
 */
public final class MechanismList {

    private MechanismList() {
        //noop
    }

    /**
     * Should contain an instance of all possible MechanismInfos.
     * @return The list of MechanismInfos.
     */
    public static MechanismInfo[] getAllMechanisms() {
        return new MechanismInfo[] {
                new OathInfo(),
                new PushInfo()
        };
    }
}
