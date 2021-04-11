package com.sprintnews.domain.model;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StructuredUserStoryTest {

    @Test
    public void givenACorrectUserStory_WhenParsingContent_ThenCorrectUserIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"As a Manager, I want a detailed cost report, so that I can plan better."});
        assertThat(sus.getUserGroup(), is(equalTo("Manager")));
    }

    @Test
    public void givenAnIncorrectUserStory_WhenParsingContent_ThenNoUserIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"Manager wants a detailed cost report, so that he can plan better."});
        assertThat(sus.getUserGroup(), is(emptyString()));
        Assertions.assertFalse(sus.isComplete());
    }

    @Test
    public void givenACorrectUserStory_WhenParsingContent_ThenCorrectRequestIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"As a Manager, I want a detailed cost report, so that I can plan better."});
        assertThat(sus.getRequest(), is(equalTo("a detailed cost report")));
    }

    @Test
    public void givenACorrectUserStoryWithoutBenefit_WhenParsingContent_ThenCorrectRequestIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"As a Manager, I want a detailed cost report."});
        assertThat(sus.getRequest(), is(equalTo("a detailed cost report")));
    }

    @Test
    public void givenAnIncorrectUserStory_WhenParsingContent_ThenNeitherUserNorRequestAreExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"Manager wants a detailed cost report"});
        assertThat(sus.getRequest(), is(emptyString()));
        Assertions.assertFalse(sus.isComplete());
    }

    @Test
    public void givenACorrectUserStory_WhenParsingContent_ThenCorrectBenefitIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"As a Manager, I want a detailed cost report, so that I can plan better."});
        assertThat(sus.getBenefit(), is(equalTo("I can plan better")));
    }

    @Test
    public void givenAUserStoryWithLongContent_WhenParsingContent_ThenCorrectBenefitIsExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{
                "This is a line at the start.",
                "As a Manager, I want a detailed cost report, so that I can plan better.",
                "This is a high prio story."});
        assertThat(sus.getBenefit(), is(equalTo("I can plan better")));
    }

    @Test
    public void givenAnIncorrectUserStory_WhenParsingContent_ThenBenefitIsNotExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"Manager wants a detailed cost report"});
        assertThat(sus.getBenefit(), is(emptyString()));
        Assertions.assertFalse(sus.isComplete());
    }

    @Test
    public void givenACorrectUserStoryWithMultipleLineBreaks_WhenParsingContent_ThenCorrectValuesAreExtracted() {
        StructuredUserStory sus = StructuredUserStory.parse("key", "title", new String[]{"As an Administrator,\n" +
                "\n" +
                ",,,i Want to be able to preview financial reports on the app, so that\n" +
                "\n" +
                "i can avoid printing too much." +
                "\n" +
                "\n"});

        // As a Finance team, i want to be able to preview financial reports on the app, so that i can avoid printing too much.
        assertThat(sus.getUserGroup(), is(equalTo("Administrator")));
        assertThat(sus.getRequest(), is(equalTo("to be able to preview financial reports on the app")));
        assertThat(sus.getBenefit(), is(equalTo("i can avoid printing too much")));
    }

}


