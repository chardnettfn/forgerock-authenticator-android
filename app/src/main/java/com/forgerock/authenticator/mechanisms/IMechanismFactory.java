package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.Map;

/**
 * Mechanism specific factory which can convert the two storage methods to a Mechanism.
 */
public interface IMechanismFactory {

    /**
     * Convert a URL to the Mechanism it contains, including extracting the owner.
     * @param uri The URI to process.
     * @return The created Mechanism.
     * @throws URIMappingException If the URL was not parsed correctly.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    Mechanism get(String uri) throws URIMappingException, MechanismCreationException;


    /**
     * Uses the map to create the Mechanism associated with the version provided, and
     * links it to the owner.
     * @param version The version of the Mechanism being created.
     * @param owner The owner of the mechanism.
     * @param map The set of properties.
     * @return The create Mechanism.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    Mechanism get(int version, Identity owner, Map<String, String> map) throws MechanismCreationException;
}
