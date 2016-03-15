package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.TOTP.Token;
import com.forgerock.authenticator.mechanisms.TOTP.TokenFactory;
import com.forgerock.authenticator.utils.OTPAuthMapper;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.HashMap;
import java.util.Map;

public class MechanismFactory {
    private Map<String, IMechanismFactory> factories;

    public MechanismFactory() {
        factories = new HashMap<>();
        addFactory(new TokenFactory());

    }

    private void addFactory(IMechanismFactory factory) {
        factories.put(factory.getMechanismString(), factory);
    }


    //Note: This should establish and populate the owner identity
    public Mechanism get(String uri) throws URIMappingException {
        // pull out type and version, then pass to correct constructor
        return factories.get(new TokenFactory().getMechanismString()).get(uri);
    }

    //Note: This should probably also take the owner identity
    public Mechanism get(String type, int version, Identity owner, Map<String, String> map) {
        // pull out type and version, then pass to correct constructor/gson?
        return factories.get(type).get(version, owner, map);
    }
}
