package com.swedbank.account.application.util;

import java.security.SecureRandom;

public class AccountNumberGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateFormattedNumber() {
        int part1 = 1000 + SECURE_RANDOM.nextInt(9000);
        int part2 = 1000 + SECURE_RANDOM.nextInt(9000);
        int part3 = 1000 + SECURE_RANDOM.nextInt(9000);

        return String.format("EE-%d-%d-%d", part1, part2, part3);
    }

}
