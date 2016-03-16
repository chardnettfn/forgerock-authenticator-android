package com.forgerock.authenticator.mechanisms;

/**
 * Base class for Mechanism layouts, which handles all aspects relating to display.
 */
public interface MechanismLayout<T extends Mechanism> {

    /**
     * Sets up this layout to represent the provided mechanism.
     * @param mechanism The mechanism to display.
     */
    void bind(T mechanism);
}
