package com.shopping.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OrderCodeGenerator() {}

    public static String generateOrderCode() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }
}
