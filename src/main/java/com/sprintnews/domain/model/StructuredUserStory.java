package com.sprintnews.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class StructuredUserStory {

    private final String key;
    private final String title;
    private final String userGroup;
    private final String request;
    private final String benefit;

    public StructuredUserStory(String key, String title, String userGroup, String request, String benefit) {
        this.key = key;
        this.title = title;
        this.userGroup = userGroup != null ? StringUtils.capitalize(userGroup.trim()) : "";
        this.request = request;
        this.benefit = benefit;
    }


    // TODO: sentence should have been cleaned up
    public static int[][] findPatternIndices(String sentence, String[][] patterns) {
        int[][] results = new int[0][];
        if (StringUtils.isBlank(sentence) || patterns == null || patterns.length == 0)
            return results;

        // Map containing a set of patterns as Key and the longest length of them as Value. Order of the patterns must be kept.
        Map<Set<String>, Integer> patternsLookup = new LinkedHashMap<>();
        for (String[] strings : patterns) {
            Set<String> currPattern = new HashSet<>();
            int longestPattern = 0;
            for (String pattern : strings) {
                currPattern.add(pattern);
                longestPattern = Math.max(longestPattern, pattern.length());
            }
            patternsLookup.put(currPattern, longestPattern);
        }

        int longestPatternFound = 0;
        int savedStart = 0;
        for (Map.Entry<Set<String>, Integer> currentPattern : patternsLookup.entrySet()) {
            int windowStart = savedStart + longestPatternFound;
            longestPatternFound = 0;
            savedStart = 0;
            boolean patternFound = false;
            for ( ; windowStart < sentence.length(); windowStart++) {
                for (String patternLookup : currentPattern.getKey()) {
                    if (windowStart + patternLookup.length() > sentence.length())
                        continue;
                    String text = sentence.substring(windowStart, windowStart + patternLookup.length());
                    if (patternLookup.equalsIgnoreCase(text)) {
                        if (text.length() > longestPatternFound) {
                            longestPatternFound = text.length();
                            savedStart = windowStart;
                        }
                        // This check can be stopped after finding the longest pattern
                        if (longestPatternFound == currentPattern.getValue()) {
                            patternFound = true;
                            break;
                        }
                    }
                }
                if (patternFound)
                    break;
            }
            // Must find one pattern to go to the next
            if (longestPatternFound == 0)
                break;

            results = Arrays.copyOf(results, results.length + 1);
            results[results.length-1] = new int[]{ Math.max(savedStart -1, 0), savedStart + longestPatternFound };
        }

        return results;
    }

    /*
        Given a list of sentences, get indices associated with pre-determined patterns (As a <user>, I want to <request> so that <benefit>).
        Sentence containing all the patterns will have priority, but it's possible to return a valid story only containing user and request.
        Using the indices, this algorithm will discard non-alphabetic characters to "clean" the sentences, e.g. removing punctuation or line breaks.
     */
    public static List<String> parseSentences(String[] contentSentences) {
        String[] patternUserGroup = new String[]{"As an", "As a", "As"};
        String[] patternRequest = new String[]{"I want"};
        String[] patternBenefit = new String[]{"so that"};
        String[][] patterns = new String[][] { patternUserGroup, patternRequest, patternBenefit };
        int[][] candidate = new int[0][0];
        int candidateSentence = -1;
        for (int i = 0; i < contentSentences.length; i++) {
            String contentSentence = contentSentences[i];
            int[][] curr = findPatternIndices(contentSentence, patterns);
            if (curr.length > candidate.length) {
                candidate = curr;
                candidateSentence = i;
                // Stop as soon as a full match is found
                if (candidate.length == 3)
                    break;
            }
        }

        // If it didn't find anything, or only the user group...
        if (candidate.length <= 1)
            return new ArrayList<>();
        else {

            String sentence = contentSentences[candidateSentence];

            // Add a final pair of indices to signal the end of the sentence
            int[][] indices = Arrays.copyOf(candidate, candidate.length + 1);
            indices[indices.length-1] = new int[] { sentence.length()-1, sentence.length()-1 };

            List<String> results = new ArrayList<>();
            // Find the words between the indices, discarding non-alpha characters
            for (int i = 0; i < indices.length - 1; i++) {
                int windowStart = indices[i][1];
                int windowEnd = indices[i+1][0];
                while (!Character.isAlphabetic(sentence.charAt(windowStart)) && windowStart < windowEnd)
                    windowStart++;
                while (!Character.isAlphabetic(sentence.charAt(windowEnd)) && windowEnd > windowStart)
                    windowEnd--;
                if (windowEnd > windowStart)
                    results.add(sentence.substring(windowStart, windowEnd+1));
            }

            return results;
        }
    }

    public static StructuredUserStory parse(String key, String title, String[] contentSentences) {
        List<String> content = parseSentences(contentSentences);
        if (content.size() <= 1)
            return emptyUserStory(key, title);

        return new StructuredUserStory(key, title, content.get(0), content.get(1), content.size() == 3 ? content.get(2) : "");
    }

    public static StructuredUserStory emptyUserStory(String key, String title) {
        return new StructuredUserStory(key, title, "", "", "");
    }

    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }
    public String getUserGroup() {
        return userGroup;
    }
    public String getRequest() {
        return request;
    }
    public String getBenefit() {
        return benefit;
    }
    public String getStoryIdAndTitle() { return String.format("%s-%s", key, title); }

    public boolean isComplete() {
        return (userGroup != null && !userGroup.trim().isEmpty()) &&
                (request != null && !request.trim().isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructuredUserStory that = (StructuredUserStory) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(title, that.title) &&
                Objects.equals(userGroup, that.userGroup) &&
                Objects.equals(request, that.request) &&
                Objects.equals(benefit, that.benefit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, title, userGroup, request, benefit);
    }

    @Override
    public String toString() {
        return "StructuredUserStory{" +
                "key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", userGroup='" + userGroup + '\'' +
                ", request='" + request + '\'' +
                ", benefit='" + benefit + '\'' +
                '}';
    }
}
