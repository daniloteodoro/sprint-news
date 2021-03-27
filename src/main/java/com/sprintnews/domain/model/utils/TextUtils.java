package com.sprintnews.domain.model.utils;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class TextUtils {
    private static final Random randomizer = new Random();

    public static <T> String generateHumanReadableList(List<T> list, Function<T, String> extractProperty) {
        if (list == null || list.isEmpty())
            return "";
        if (list.size() == 1)
            return extractProperty.apply(list.get(0));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String curr = extractProperty.apply(list.get(i));
            if (i < list.size() - 2)
                sb.append(curr)
                        .append(", ");
            else if (i == list.size() - 2)
                sb.append(curr)
                        .append(" and ");
            else
                sb.append(curr);
        }
        return sb.toString();
    }

    /**
     * Removes the period/full stop that ends the sentence. Ignores cases where it is actually ellipsis.
     * @param original the sentence to be analyzed
     * @return the sentence without its period.
     */
    public static String removeFinalPunctuation(String original) {
        if (original == null)
            return "";

        String trimmedSentence = original.trim();
        if (trimmedSentence.isEmpty())
            return "";

        char last = trimmedSentence.charAt(trimmedSentence.length() - 1);

        if (last != '.' || trimmedSentence.endsWith("..."))
            return trimmedSentence;

        return trimmedSentence.substring(0, trimmedSentence.length() - 1);
    }

    public static <T> T getNextRandomItemFrom(List<T> list) {
        if (list == null || list.isEmpty())
            return null;
        return list.get(randomizer.nextInt(list.size()));
    }

    public static String addDoubleQuotes(String original) {
        return String.format("\"%s\"", original);
    }

    public static boolean isBlank(String text) {
        return text == null ||
                text.trim().isEmpty();
    }

    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

}
