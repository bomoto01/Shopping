package com.shopping.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-{2,}");

    private SlugUtil() {}

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace("đ", "d").replace("Đ", "d");

        String nfdNormalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noDiacritics = pattern.matcher(nfdNormalized).replaceAll("");

        String noWhitespace = WHITESPACE.matcher(noDiacritics).replaceAll("-");
        String normalizedSlug = NONLATIN.matcher(noWhitespace).replaceAll("");
        normalizedSlug = MULTI_HYPHEN.matcher(normalizedSlug).replaceAll("-");

        if (normalizedSlug.startsWith("-")) {
            normalizedSlug = normalizedSlug.substring(1);
        }
        if (normalizedSlug.endsWith("-")) {
            normalizedSlug = normalizedSlug.substring(0, normalizedSlug.length() - 1);
        }

        return normalizedSlug;
    }
}
