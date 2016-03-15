package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.forgerock.authenticator.utils.URIMappingException;

import java.util.Map;

public interface IMechanismFactory {
    String getMechanismString();

    Mechanism get(String uri) throws URIMappingException, MechanismCreationException;

    Mechanism get(int version, Identity owner, Map<String, String> map) throws MechanismCreationException;
}
