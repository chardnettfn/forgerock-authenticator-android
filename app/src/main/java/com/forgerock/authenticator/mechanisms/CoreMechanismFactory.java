package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Determines the type of mechanism which is being created, and routes the creation request to the
 * appropriate builder.
 */
public class CoreMechanismFactory {
    private Map<String, IMechanismFactory> factories;

    public CoreMechanismFactory() {
        factories = new HashMap<>();
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            addFactory(info);
        }
    }

    private void addFactory(MechanismInfo info) {
        factories.put(info.getMechanismString(), info.getFactory());
    }

    /**
     * Convert a URL to the Mechanism it contains, including extracting the owner.
     * @param uri The URI to process.
     * @return The created Mechanism.
     * @throws URIMappingException If the URL was not parsed correctly.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public Mechanism get(String uri) throws URIMappingException, MechanismCreationException {
        // pull out type, then pass to correct constructor (currently assumes it's a token)
        for (MechanismInfo info : MechanismList.getAllMechanisms()) {
            if (info.matchesURI(uri)) {
                return factories.get(info.getMechanismString()).get(uri);
            }
        }
        throw new MechanismCreationException("Unknown URI structure");
    }

    /**
     * Uses the map to create the Mechanism associated with the type and version provided, and
     * links it to the owner.
     * @param type The mechanism string for the Mechanism.
     * @param version The version of the Mechanism being created.
     * @param owner The owner of the mechanism.
     * @param map The set of properties.
     * @return The create Mechanism.
     * @throws MechanismCreationException If the data was not valid to create a Mechanism.
     */
    public Mechanism get(String type, int version, Identity owner, Map<String, String> map) throws MechanismCreationException {
        return factories.get(type).get(version, owner, map);
    }
}
