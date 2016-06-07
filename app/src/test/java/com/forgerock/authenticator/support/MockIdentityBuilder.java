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

package com.forgerock.authenticator.support;

import android.net.Uri;

import com.forgerock.authenticator.identity.Identity;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MockIdentityBuilder {
    private Identity identity;

    public MockIdentityBuilder() {
        identity = mock(Identity.class);
    }

    public MockIdentityBuilder withIssuer(String issuer) {
        given(identity.getIssuer()).willReturn(issuer);
        return this;
    }

    public MockIdentityBuilder withAccountName(String accountName) {
        given(identity.getAccountName()).willReturn(accountName);
        return this;
    }

    public MockIdentityBuilder withBackgroundColor(String backgroundColor) {
        given(identity.getBackgroundColor()).willReturn(backgroundColor);
        return this;
    }

    public MockIdentityBuilder withImageUri(Uri imageUri) {
        given(identity.getImageURL()).willReturn(imageUri);
        return this;
    }

    public Identity build() {
        Identity result = identity;
        identity = null;
        return result;
    }
}
