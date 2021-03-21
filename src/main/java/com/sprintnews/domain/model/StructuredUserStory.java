package com.sprintnews.domain.model;

import org.apache.commons.lang3.StringUtils;

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

    private static String findFirstCandidateSentence(String[] contentSentences) {
        if (contentSentences == null || contentSentences.length == 0)
            return "";

        if (contentSentences.length == 1)
            return contentSentences[0];

        final String USER_STORY_START_1 = "As a ";
        final String USER_STORY_START_2 = "As ";
        final int NON_EXISTING = -1;

        for (String sentence : contentSentences) {
            int stakeholderStart = sentence.indexOf(USER_STORY_START_1);
            if (stakeholderStart == NON_EXISTING)
                stakeholderStart = sentence.indexOf(USER_STORY_START_2);

            if (stakeholderStart != NON_EXISTING)
                return sentence;
        }

        return "";
    }

    public static StructuredUserStory parse(String key, String title, String[] contentSentences) {
        int NON_EXISTING = -1;
        String USER_STORY_START_1 = "As a ";
        String USER_STORY_START_2 = "As ";
        String USER_STORY_REQUEST_START_1 = ", I want ";
        String USER_STORY_REQUEST_START_2 = ",I want ";
        String USER_STORY_REQUEST_START_3 = "I want ";
        String USER_STORY_BENEFIT_START_1 = ",so that ";
        String USER_STORY_BENEFIT_START_2 = ", so that ";
        String USER_STORY_BENEFIT_START_3 = "so that";

        String sentence = findFirstCandidateSentence(contentSentences);
        if ("".equals(sentence))
            return emptyUserStory(key, title);

        // Define Stakeholder extraction positions
        int stakeholderStart = sentence.indexOf(USER_STORY_START_1);
        int stakeholderStartLength = USER_STORY_START_1.length();
        if (stakeholderStart == NON_EXISTING) {
            stakeholderStart = sentence.indexOf(USER_STORY_START_2);
            stakeholderStartLength = USER_STORY_START_2.length();
        }

        // Define Request extraction positions
        int requestStart = sentence.indexOf(USER_STORY_REQUEST_START_1, stakeholderStart);
        int requestStartLength = USER_STORY_REQUEST_START_1.length();
        if (requestStart == NON_EXISTING) {
            requestStart = sentence.indexOf(USER_STORY_REQUEST_START_2, stakeholderStart);
            requestStartLength = USER_STORY_REQUEST_START_2.length();
        }
        if (requestStart == NON_EXISTING) {
            requestStart = sentence.indexOf(USER_STORY_REQUEST_START_3, stakeholderStart);
            requestStartLength = USER_STORY_REQUEST_START_3.length();
        }

        if (requestStart == NON_EXISTING)
            return emptyUserStory(key, title);

        // Stakeholder: Copy from index ["As a "|"As "] until [",I want ", ", I want ", "I want "]
        String stakeholder = sentence.substring(stakeholderStart + stakeholderStartLength, requestStart);
        if (stakeholder.isEmpty())
            return emptyUserStory(key, title);

        // Define Benefit extraction positions
        int benefitStart = sentence.indexOf(USER_STORY_BENEFIT_START_1, requestStart);
        int benefitStartLength = USER_STORY_BENEFIT_START_1.length();
        if (benefitStart == NON_EXISTING) {
            benefitStart = sentence.indexOf(USER_STORY_BENEFIT_START_2, requestStart);
            benefitStartLength = USER_STORY_BENEFIT_START_2.length();
        }
        if (benefitStart == NON_EXISTING) {
            benefitStart = sentence.indexOf(USER_STORY_BENEFIT_START_3, requestStart);
            benefitStartLength = USER_STORY_BENEFIT_START_3.length();
        }

        if (benefitStart == NON_EXISTING) {
            // Request goes until the end of the sentence in case of no benefit section in the user story.
            benefitStart = sentence.length();
            benefitStartLength = 0;
        }

        // Request: Copy from index from (previous) index2+length until [",so that ", ", so that ", "so that"]
        String request = sentence.substring(requestStart + requestStartLength, benefitStart);
        if (request.isEmpty())
            return emptyUserStory(key, title);

        // Benefit formula: Copy fro index from (previous) index4+searchwordlength until the end of the sentence (via opennlp or \n)
        String benefit = sentence.substring(benefitStart + benefitStartLength);

        return new StructuredUserStory(key, title, stakeholder, request, benefit);
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

}
