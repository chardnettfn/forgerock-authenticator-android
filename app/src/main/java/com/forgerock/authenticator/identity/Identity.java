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

import android.graphics.Color;
import android.net.Uri;

import com.forgerock.authenticator.mechanisms.DuplicateMechanismException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.model.SortedList;
import com.forgerock.authenticator.storage.IdentityModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Identity is responsible for modelling the information that makes up part of a users identity in
 * the context of logging into that users account.
 */
public class Identity extends ModelObject<Identity> {
    private long id = NOT_STORED;
    private final String issuer;
    private final String accountName;
    private final Uri imageURL;
    private final List<Mechanism> mechanismList;
    private static final Logger logger = LoggerFactory.getLogger(Identity.class);
    private final String backgroundColor;


    private Identity(IdentityModel model, long id, String issuer, String accountName, String backgroundColor, Uri imageURL) {
        super(model);
        this.id = id;
        this.issuer = issuer;
        this.accountName = accountName;
        this.imageURL = imageURL;
        this.mechanismList = new SortedList<>();
        this.backgroundColor = backgroundColor;
    }

    /**
     * Adds the provided mechanism to this Identity, and therefore to the larger data model.
     * @param builder An incomplete builder for a non stored mechanism.
     * @return The mechanism that has been added to the data model.
     * @throws MechanismCreationException If something went wrong when creating the mechanism.
     */
    public Mechanism addMechanism(Mechanism.PartialMechanismBuilder builder) throws MechanismCreationException {
        Mechanism mechanism = builder.build(this);
        if (!mechanism.isStored()) {
            Mechanism duplicate = findMatching(mechanism);
            if (duplicate != null) {
                throw new DuplicateMechanismException("Tried to add duplicate mechanism to identity", duplicate);
            } else {
                mechanism.save();
                mechanismList.add(mechanism);
            }
        } else {
            throw new MechanismCreationException("Tried to add previously saved mechanism to identity");
        }
        return mechanism;
    }

    private Mechanism findMatching(Mechanism other) {
        for (Mechanism mechanism : mechanismList) {
            if (mechanism.matches(other)) {
                return mechanism;
            }
        }
        return null;
    }

    /**
     * Deletes the provided mechanism, and removes it from this Identity. Deletes this identity if
     * this results in this identity containing no mechanisms.
     * @param mechanism The mechanism to delete.
     */
    public void removeMechanism(Mechanism mechanism) {
        mechanism.delete();
        mechanismList.remove(mechanism);

        if (mechanismList.isEmpty()) {
            getModel().removeIdentity(this);
        }
    }

    /**
     * Gets all of the mechanisms that belong to this Identity.
     * @return The list of mechanisms.
     */
    public List<Mechanism> getMechanisms() {
        return Collections.unmodifiableList(mechanismList);
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
     * @return The account name.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the image URL for the IDP that issued this identity.
     * @return Non null {@link Uri} representing the path to the image, or null if not assigned.
     */
    public Uri getImageURL() {
        return imageURL;
    }

    /**
     * Gets the background color for the IDP that issued this identity.
     * @return A hex string including a prepending # symbol, representing the color (e.g. #aabbcc)
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public ArrayList<String> getOpaqueReference() {
        ArrayList<String> reference = new ArrayList<>();
        reference.add(issuer + ":" + accountName);
        return reference;
    }

    @Override
    public boolean consumeOpaqueReference(ArrayList<String> reference) {
        if (reference != null && reference.size() > 0 && reference.get(0) != null &&
                reference.get(0).equals(issuer + ":" + accountName)) {
            reference.remove(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (Mechanism mechanism : mechanismList) {
            valid = valid && mechanism.validate();
        }
        return isStored() && valid;
    }

    @Override
    public boolean isStored() {
        return id != NOT_STORED;
    }

    @Override
    public void save() {
        if (!isStored()) {
            id = getModel().getStorageSystem().addIdentity(this);
        } else {
            // TODO: handle updates
        }
    }

    @Override
    public boolean forceSave() {
        id = getModel().getStorageSystem().addIdentity(this);
        return id != NOT_STORED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identity identity = (Identity) o;

        if (!issuer.equals(identity.issuer)) return false;
        if (!accountName.equals(identity.accountName)) return false;
        if (imageURL != null ? !imageURL.equals(identity.imageURL) : identity.imageURL != null)
            return false;
        return !(backgroundColor != null ? !backgroundColor.equals(identity.backgroundColor) : identity.backgroundColor != null);

    }

    @Override
    public int hashCode() {
        int result = issuer.hashCode();
        result = 31 * result + accountName.hashCode();
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        return result;
    }

    @Override
    public void delete() {
        for (Mechanism mechanism : mechanismList) {
            mechanism.delete();
        }
        if (id != NOT_STORED) {
            getModel().getStorageSystem().deleteIdentity(id);
            id = NOT_STORED;
        }

    }

    @Override
    public boolean matches(Identity other) {
        if (other == null) {
            return false;
        }
        return other.issuer.equals(issuer) && other.accountName.equals(accountName);
    }

    /**
     * Returns a builder for creating an Identity.
     * @return The Identity builder.
     */
    public static IdentityBuilder builder() {
        return new IdentityBuilder();
    }

    private void populateMechanisms(List<Mechanism.PartialMechanismBuilder> mechanismBuilders) {
        for (Mechanism.PartialMechanismBuilder mechanismBuilder : mechanismBuilders) {
            try {
                Mechanism mechanism = mechanismBuilder.build(this);
                if (mechanism.isStored()) {
                    mechanismList.add(mechanism);
                } else {
                    logger.error("Tried to populate mechanism list with Mechanism that has not been stored.");
                }
            } catch (MechanismCreationException e) {
                logger.error("Something went wrong while loading Mechanism.", e);
            }
        }
        Collections.sort(mechanismList);
    }

    @Override
    public int compareTo(Identity another) {
        if (another == null) {
            return -1;
        }
        int compareIssuer = issuer.compareTo(another.issuer);
        if (compareIssuer == 0) {
            return accountName.compareTo(another.accountName);
        }
        return compareIssuer;
    }

    /**
     * Builder class responsible for producing Identities.
     */
    public static class IdentityBuilder {
        private long id = NOT_STORED;
        private String issuer = "";
        private String accountName = "";
        private Uri imageURL;
        private List<Mechanism.PartialMechanismBuilder> mechanismBuilders = new ArrayList<>();
        private String backgroundColor;

        /**
         * Sets the storage id of this Identity. Should not be set manually, or if the Identity is
         * not stored.
         * @param id The storage id.
         */
        public IdentityBuilder setId(long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the name of the IDP that issued this identity.
         * @param issuer The IDP name.
         */
        public IdentityBuilder setIssuer(String issuer) {
            this.issuer = issuer != null ? issuer : "";
            return this;
        }

        /**
         * Sets the name of the identity.
         * @param accountName The identity name.
         */
        public IdentityBuilder setAccountName(String accountName) {
            this.accountName = accountName != null ? accountName : "";
            return this;
        }

        /**
         * Sets the imageURL for the IDP that issued this identity.
         * @param imageURL A string that represents the image URI.
         */
        public IdentityBuilder setImageURL(String imageURL) {
            this.imageURL = imageURL == null ? null : Uri.parse(imageURL);
            return this;
        }

        /**
         * Sets the mechanisms that are currently associated with this Identity.
         * @param mechanismBuilders A list of incomplete mechanism builders.
         */
        public IdentityBuilder setMechanisms(List<Mechanism.PartialMechanismBuilder> mechanismBuilders) {
            this.mechanismBuilders = mechanismBuilders;
            return this;
        }

        public IdentityBuilder setBackgroundColor(String color) {
            try {
                if (color != null) {
                    Color.parseColor(color);
                    backgroundColor = color;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Tried to parse invalid string as color ({})", color, e);
            }
            return this;
        }

        /**
         * Produces the Identity object that was being constructed.
         * @return The identity.
         */
        public Identity build(IdentityModel model) {
            Identity result =  new Identity(model, id, issuer, accountName, backgroundColor, imageURL);
            result.populateMechanisms(mechanismBuilders);
            return result;
        }
    }
}