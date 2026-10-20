package com.financeapp.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isStrongPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static BigDecimal parsePositiveAmount(String text) {
        if (text == null) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(text.trim());
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                return amount;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}

