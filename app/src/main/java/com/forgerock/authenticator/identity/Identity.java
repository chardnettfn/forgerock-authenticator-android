package com.forgerock.authenticator.identity;

import android.net.Uri;

import com.forgerock.authenticator.mechanisms.Mechanism;

import java.util.ArrayList;
import java.util.List;

public class Identity {
    private String issuer;
    private String label;
    private String image;

    public String getIssuer() {
        return issuer;
    }

    // NOTE: This changes internal data. You MUST save the token immediately.
    public void setIssuer(String updatedIssuer) {
        this.issuer = updatedIssuer;
    }

    /**
     * @return The label if it has been assigned or an empty String.
     */
    public String getLabel() {
        return label != null ? label : "";
    }

    // NOTE: This changes internal data. You MUST save the token immediately.
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Non null {@link Uri} representing the path to the image, or null if not assigned.
     */
    public Uri getImage() {
        return image == null ? null : Uri.parse(image);
    }

    public void setImage(String image) {
        this.image = image;
    }
}