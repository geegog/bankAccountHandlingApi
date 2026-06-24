package com.swedbank.account.application.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class AccountNumberGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int ACCOUNT_NUMBER_LENGTH = 12;


    public static String generateAccountNumber() {
        return SECURE_RANDOM.ints(ACCOUNT_NUMBER_LENGTH, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

}
