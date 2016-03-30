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

package com.forgerock.authenticator.identity;

import android.net.Uri;

import com.forgerock.authenticator.storage.NotStoredException;

/**
 * Identity is responsible for modelling the information that makes up part of a users identity in
 * the context of logging into that users account.
 */
public class Identity {
    private long id = -1;
    private String issuer;
    private String accountName;
    private Uri image;

    private Identity(String issuer, String accountName, Uri image) {
        this.issuer = issuer;
        this.accountName = accountName;
        this.image = image;
    }

    /**
     * Returns the id that corresponds to this Identity.
     * @return The storage id that relates to this Identity.
     */
    public long getId() throws NotStoredException {
        if (id == -1) {
            throw new NotStoredException("The mechanism has not yet been stored.");
        }
        return id;
    }

    /**
     * Sets the storage id for this Identity.
     * @param id The id that the storage mechanism uses to identify this Identity.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns a builder for creating an Identity.
     * @return The Identity builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the name of the IDP that issued this identity.
     * @return The name of the IDP.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Returns the name of this Identity.
     * @return The account name if it has been assigned or an empty String.
     */
    public String getAccountName() {
        return accountName != null ? accountName : "";
    }

    /**
     * Gets the image for the IDP that issued this identity.
     * @return Non null {@link Uri} representing the path to the image, or null if not assigned.
     */
    public Uri getImage() {
        return image;
    }

    /**
     * Builder class responsible for producing Identities.
     */
    public static class Builder {
        private String issuer;
        private String accountName;
        private Uri image;

        /**
         * Sets the name of the IDP that issued this identity.
         * @param issuer The IDP name.
         */
        public Builder setIssuer(String issuer) {
            this.issuer = issuer != null ? issuer : "";
            return this;
        }

        /**
         * Sets the name of the identity.
         * @param accountName The identity name.
         */
        public Builder setAccountName(String accountName) {
            this.accountName = accountName != null ? accountName : "";
            return this;
        }

        /**
         * Sets the image for the IDP that issued this identity.
         * @param image A string that represents the image URI.
         */
        public Builder setImage(String image) {
            this.image = image == null ? null : Uri.parse(image);
            return this;
        }

        /**
         * Produces the Identity object that was being constructed.
         * @return The identity.
         */
        public Identity build() {
            return new Identity(issuer, accountName, image);
        }
    }
}