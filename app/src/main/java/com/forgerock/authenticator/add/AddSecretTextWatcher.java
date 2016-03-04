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

package com.forgerock.authenticator.add;

import android.app.Activity;
import android.text.Editable;

public class AddSecretTextWatcher extends AddTextWatcher {
    public AddSecretTextWatcher(Activity activity) {
        super(activity);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() != 0) {
            // Ensure that = is only permitted at the end
            boolean haveData = false;
            for (int i = s.length() - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (c != '=')
                    haveData = true;
                else if (haveData)
                    s.delete(i, i + 1);
            }
        }

        super.afterTextChanged(s);
    }
}
