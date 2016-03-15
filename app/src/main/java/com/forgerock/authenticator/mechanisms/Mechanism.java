package com.forgerock.authenticator.mechanisms;

import com.forgerock.authenticator.identity.Identity;

import java.util.Map;

public interface Mechanism {
    int getVersion();
    long getRowId();
    void setRowId(long rowId);
    Map<String, String> asMap();
    IMechanismFactory getFactory();
    MechanismLayoutManager getLayoutManager();
    Identity getOwner();



}
