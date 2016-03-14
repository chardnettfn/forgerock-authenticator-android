/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.mechanisms.TOTP;

import android.net.Uri;
import com.google.android.apps.authenticator.Base32String;
import com.google.android.apps.authenticator.Base32String.DecodingException;
import com.forgerock.authenticator.utils.URIMappingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Responsible for representing a Token in the authenticator application. Combines a
 * number of responsibilities into the same class.
 *
 * - Generate new OTP codes
 * - Value object for display purposes
 */
public class Token {
    public enum TokenType {
        HOTP, TOTP
    }

    private String issuer;
    private String label;
    private String image;
    private TokenType type;
    private String algo;
    private byte[] secret;
    private int digits;
    private long counter;
    private int period;

    private String getHOTP(long counter) {
        // Encode counter in network byte order
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(counter);

        // Create digits divisor
        int div = 1;
        for (int i = digits; i > 0; i--)
            div *= 10;

        // Create the HMAC
        try {
            Mac mac = Mac.getInstance("Hmac" + algo);
            mac.init(new SecretKeySpec(secret, "Hmac" + algo));

            // Do the hashing
            byte[] digest = mac.doFinal(bb.array());

            // Truncate
            int binary;
            int off = digest[digest.length - 1] & 0xf;
            binary = (digest[off] & 0x7f) << 0x18;
            binary |= (digest[off + 1] & 0xff) << 0x10;
            binary |= (digest[off + 2] & 0xff) << 0x08;
            binary |= (digest[off + 3] & 0xff);
            binary = binary % div;

            // Zero pad
            String hotp = Integer.toString(binary);
            while (hotp.length() != digits)
                hotp = "0" + hotp;

            return hotp;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getID() {
        if (!issuer.isEmpty()) {
            return issuer + ":" + label;
        }
        return label;
    }

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

    public int getDigits() {
        return digits;
    }

    // NOTE: This may change internal data. You MUST save the token immediately.
    public TokenCode generateCodes() {
        long cur = System.currentTimeMillis();

        switch (type) {
        case HOTP:
            counter++;
            return new TokenCode(getHOTP(counter), cur, cur + (period * 1000));

        case TOTP:
            long counter = cur / 1000 / period;
            return new TokenCode(getHOTP(counter + 0),
                                 (counter + 0) * period * 1000,
                                 (counter + 1) * period * 1000,
                   new TokenCode(getHOTP(counter + 1),
                                 (counter + 1) * period * 1000,
                                 (counter + 2) * period * 1000));
        }

        return null;
    }

    public TokenType getType() {
        return type;
    }

    /**
     * @param type Type must be 'totp' or 'hotp'.
     * @throws URIMappingException If the value was not permitted.
     */
    public void setType(String type) throws URIMappingException {
        try {
            this.type = TokenType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new URIMappingException("Invalid type: " + type);
        }
    }

    /**
     * Assumption: algorithm name is valid if a corresponding algorithm can be loaded.
     *
     * @param algorithm Non null algorithm to assign.
     * @throws URIMappingException If the value was not a supported algorithm.
     */
    public void setAlgorithm(String algorithm) throws URIMappingException {
        String algoUpperCase = algorithm.toUpperCase(Locale.US);
        try {
            Mac.getInstance("Hmac" + algoUpperCase);
            algo = algoUpperCase;
        } catch (NoSuchAlgorithmException e1) {
            throw new URIMappingException("Invalid algorithm: " + algorithm);
        }
    }

    /**
     * @param digitStr Non null digits string, either 6 or 8.
     * @throws URIMappingException If the value did not match allowed values.
     */
    public void setDigits(String digitStr) throws URIMappingException {
        try {
            this.digits = Integer.parseInt(digitStr);
            if (digits != 6 && digits != 8) {
                throw new URIMappingException("Digits must be 6 or 8: " + digitStr);
            }
        } catch (NumberFormatException e) {
            throw new URIMappingException("Digits was not a number: " + digitStr);
        }
    }

    /**
     * @param periodStr Non null period in seconds.
     * @throws URIMappingException If the value was not a number.
     */
    public void setPeriod(String periodStr) throws URIMappingException {
        try {
            this.period = Integer.parseInt(periodStr);
        } catch (NumberFormatException e) {
            throw new URIMappingException("Period was not a number: " + periodStr);
        }
    }

    /**
     * Base32 encodeding based on: http://tools.ietf.org/html/rfc4648#page-8
     *
     * @param secretStr A non null Base32 encoded secret key.
     * @throws URIMappingException If the value was not Base32 encoded.
     */
    public void setSecret(String secretStr) throws URIMappingException {
        try {

            secret = Base32String.decode(secretStr);
        } catch (DecodingException e) {
            throw new URIMappingException("Could not decode secret: " + secretStr, e);
        } catch (NullPointerException e) {
            throw new URIMappingException("Unexpected null whilst parsing secret: " + secretStr, e);
        }
    }

    /**
     * @param counterStr Non null counter as an integer.
     * @throws URIMappingException If the counter string was not a number.
     */
    public void setCounter(String counterStr) throws URIMappingException {
        try {
            counter = Long.parseLong(counterStr);
        } catch (NumberFormatException e) {
            throw new URIMappingException("Failed to parse counter: " + counterStr, e);
        }
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
