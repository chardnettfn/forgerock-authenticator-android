package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;

import java.util.Map;

/**
 * A mechanism used for authentication.
 * Encapsulates the related settings, as well as an owning Identity.
 */
public interface Mechanism {
    /**
     * Gets the version number for this mechanism.
     * @return
     */
    int getVersion();

    /**
     * Gets the id of this Mechanism used for storage. This value is unique to each Mechanism instance.
     * Return -1 if the id is not set.
     * @return The id.
     */
    long getRowId();

    /**
     * Sets the id of this Mechanism. Should only be used by the storage method.
     * @param rowId
     */
    void setRowId(long rowId);

    /**
     * Returns the Mechanism's properties as a map of properties. The factory should be able to use
     * this map to recreate the Mechanism.
     * @return The Mechanism's properties.
     */
    Map<String, String> asMap();

    /**
     * Gets the MechanismInfo which describes this Mechanism.
     * @return The related MechanismInfo.
     */
    MechanismInfo getInfo();

    /**
     * Returns the identity which owns this Mechanism.
     * @return The owning identity.
     */
    Identity getOwner();
}
