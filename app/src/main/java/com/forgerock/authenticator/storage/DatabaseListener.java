package com.forgerock.authenticator.storage;

/**
 * Listens to a database for the data to change.
 */
public interface DatabaseListener {
    /**
     * Called when any write operation takes place.
     */
    void onUpdate();
}
