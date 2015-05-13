/*
 * FreeOTP
 *
 * Authors: Nathaniel McCallum <npmccallum@redhat.com>
 *
 * Copyright (C) 2013  Nathaniel McCallum, Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* Portions Copyrighted 2015 ForgeRock AS.
 */

package org.forgerock.authenticator;

import android.net.Uri;
import com.google.android.apps.authenticator.Base32String;
import com.google.android.apps.authenticator.Base32String.DecodingException;
import org.forgerock.authenticator.utils.OTPAuthMapper;
import org.forgerock.authenticator.utils.URIMappingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;

/**
 * Responsible for representing a Token in the authenticator application. Combines a
 * number of responsibilities into the same class.
 *
 * - Configuring based on configuration URI
 * - Generate new OTP codes
 * - Value object for display purposes
 */
public class Token {
    public static class TokenUriInvalidException extends Exception {
        private static final long serialVersionUID = -1108624734612362345L;
    }

    private final OTPAuthMapper mapper = new OTPAuthMapper();

    public static enum TokenType {
        HOTP, TOTP
    }

    private String issuerAlt;
    private String issuer;
    private String label;
    private String labelAlt;
    private String image;
    private String imageAlt;
    private TokenType type;
    private String algo;
    private byte[] secret;
    private int digits;
    private long counter;
    private int period;

    /**
     * Intialise the Token based on the configuration URI.
     *
     * @param uri Non null
     * @param internal True will attempt to parse extra parameters from the URI
     * @throws TokenUriInvalidException If there was any error in parsing the URI
     */
    public Token(String uri, boolean internal) throws TokenUriInvalidException {
        Map<String, String> values;
        try {
            values = mapper.map(uri);
        } catch (URIMappingException e) {
            throw new TokenUriInvalidException();
        }

        if (!"otpauth".equals(values.get(OTPAuthMapper.SCHEME))) {
            throw new TokenUriInvalidException();
        }

        if ("totp".equals(values.get(OTPAuthMapper.TYPE))) {
            type = TokenType.TOTP;
        } else if ("hotp".equals(values.get(OTPAuthMapper.TYPE))) {
            type = TokenType.HOTP;
        } else {
            throw new TokenUriInvalidException();
        }

        issuer = get(values, OTPAuthMapper.ISSUER, "");
        label = get(values, OTPAuthMapper.LABEL, "");


        algo = get(values, OTPAuthMapper.ALGORITHM, "sha1").toUpperCase(Locale.US);
        try {
            Mac.getInstance("Hmac" + algo);
        } catch (NoSuchAlgorithmException e1) {
            throw new TokenUriInvalidException();
        }

        String d = get(values, OTPAuthMapper.DIGITS, "6");
        try {
            digits = Integer.parseInt(d);
            if (digits != 6 && digits != 8) {
                throw new TokenUriInvalidException();
            }
        } catch (NumberFormatException e) {
            throw new TokenUriInvalidException();
        }

        try {
            String p = get(values, OTPAuthMapper.PERIOD, "30");
            period = Integer.parseInt(p);
        } catch (NumberFormatException e) {
            throw new TokenUriInvalidException();
        }

        if (type == TokenType.HOTP) {
            try {
                String c = get(values, OTPAuthMapper.COUNTER, "0");
                counter = Long.parseLong(c);
            } catch (NumberFormatException e) {
                throw new TokenUriInvalidException();
            }
        }

        try {
            String s = get(values, OTPAuthMapper.SECRET, "");
            secret = Base32String.decode(s);
        } catch (DecodingException e) {
            throw new TokenUriInvalidException();
        } catch (NullPointerException e) {
            throw new TokenUriInvalidException();
        }

        image = values.get("image");

        if (internal) {
            setIssuer(values.get("issueralt"));
            setLabel(values.get("labelalt"));
        }
    }

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

    public Token(Uri uri) throws TokenUriInvalidException {
        this(uri.toString(), false);
    }

    public Token(String uri) throws TokenUriInvalidException {
        this(uri, false);
    }

    public String getID() {
        if (!issuer.isEmpty()) {
            return issuer + ":" + label;
        }
        return label;
    }

    // NOTE: This changes internal data. You MUST save the token immediately.
    public void setIssuer(String updatedIssuer) {
        if (updatedIssuer != null && !issuer.equals(updatedIssuer)) {
            issuerAlt = updatedIssuer;
        }
    }

    public String getIssuer() {
        if (issuerAlt != null) {
            return issuerAlt;
        }
        return issuer;
    }

    // NOTE: This changes internal data. You MUST save the token immediately.
    public void setLabel(String label) {
        labelAlt = (label == null || label.equals(this.label)) ? null : label;
    }

    public String getLabel() {
        if (labelAlt != null)
            return labelAlt;
        return label != null ? label : "";
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

    public Uri toUri() {
        String issuerLabel = getID();

        Uri.Builder builder = new Uri.Builder().scheme("otpauth").path(issuerLabel)
                .appendQueryParameter("secret", Base32String.encode(secret))
                .appendQueryParameter("issuer", issuer)
                .appendQueryParameter("algorithm", algo)
                .appendQueryParameter("digits", Integer.toString(digits))
                .appendQueryParameter("period", Integer.toString(period));

        switch (type) {
        case HOTP:
            builder.authority("hotp");
            builder.appendQueryParameter("counter", Long.toString(counter + 1));
            break;
        case TOTP:
            builder.authority("totp");
            break;
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return toUri().toString();
    }

    public void setImage(Uri image) {
        imageAlt = null;
        if (image == null)
            return;

        if (this.image == null || !Uri.parse(this.image).equals(image))
            imageAlt = image.toString();
    }

    public Uri getImage() {
        if (imageAlt != null)
            return Uri.parse(imageAlt);

        if (image != null)
            return Uri.parse(image);

        return null;
    }

    private static String get(Map<String, String> m, String name, String def) {
        String r = m.get(name);
        return r == null ? def : r;
    }
}
