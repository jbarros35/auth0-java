package com.auth0;

import java.util.Random;

public class NonceGenerator {

    private final Random randomSource = new Random();

    // Taken from tomcat-catalina 7.0.0 CsrfPreventionFilter class

    /**
     * Generate a once time token (nonce) for authenticating subsequent
     * requests.
     * The nonce generation is a simplified version of ManagerBase.generateSessionId().
     */
    public String generateNonce() {
        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        StringBuilder buffer = new StringBuilder();

        randomSource.nextBytes(random);

        for (int j = 0; j < random.length; j++) {
            byte b1 = (byte) ((random[j] & 0xf0) >> 4);
            byte b2 = (byte) (random[j] & 0x0f);
            if (b1 < 10)
                buffer.append((char) ('0' + b1));
            else
                buffer.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                buffer.append((char) ('0' + b2));
            else
                buffer.append((char) ('A' + (b2 - 10)));
        }

        return buffer.toString();
    }

}
