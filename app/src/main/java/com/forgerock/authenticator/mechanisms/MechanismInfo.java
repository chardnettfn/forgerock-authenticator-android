package com.forgerock.authenticator.mechanisms;

import android.view.View;

/**
 * Provides information about a type of mechanism.
 */
public interface MechanismInfo {
    /**
     * Bind the mechanism to the given view (assumes that they are correctly matched).
     * @param mechanism The mechanism to bind.
     * @param view The view to bind it to.
     */
    void bind(View view, Mechanism mechanism);

    /**
     * Get the layout id for this layout type.
     * @return The layout id.
     */
    int getLayoutType();

    /**
     * Get the string used to represent this type in the database.
     * @return The mechanism string.
     */
    String getMechanismString();

    /**
     * Get the factory appropriate for this mechanism.
     * @return The mechanism factory.
     */
    IMechanismFactory getFactory();

    /**
     * Determines if the provided URI is for this type of mechnism.
     * @param uri
     * @return
     */
    boolean matchesURI(String uri);

}
