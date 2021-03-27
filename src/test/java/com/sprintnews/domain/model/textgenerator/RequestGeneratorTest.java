package com.sprintnews.domain.model.textgenerator;

import com.sprintnews.domain.model.StructuredUserStory;
import opennlp.tools.postag.POSModel;
import opennlp.tools.tokenize.DetokenizationDictionary;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Main goal here is to check how the Request Generator behaves for each specific part of speech.
 * ps: Some of the tests have (and therefore produce) grammatically strange results in order to test some part of speeches.
 * @author daniloteodoro
 */
public class RequestGeneratorTest {
    private static final String DEFAULT_USER = "User";

    private static final POSModel posModel = getPosModel();
    private static final DetokenizationDictionary detokenizerDict = getDetokenizerDictionary();

    private static POSModel getPosModel() {
        try (InputStream is = RequestGeneratorTest.class.getResourceAsStream("/models/en-pos-maxent.bin")) {
            return new POSModel(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure loading PoS tagger model");
        }
    }

    private static DetokenizationDictionary getDetokenizerDictionary() {
        try (InputStream is = RequestGeneratorTest.class.getResourceAsStream("/models/en-detokenizer.xml")) {
            return new DetokenizationDictionary(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure loading Detokenizer Dictionary");
        }
    }

    private StructuredUserStory createWithRequest(String request) {
        return StructuredUserStory.parse("key", "title", new String[]{String.format("As a User, I want %s, so that I get some benefit.", request)});
    }

    @Test
    public void generateCorrectTextGivenARequestStartingWithAdjective() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        // green, adjective (JJ)
        StructuredUserStory sus = createWithRequest("green specific report");
        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Group of stakeholders User indicated in one story that they want green specific report.")));
    }

    @Test
    public void generateCorrectTextGivenARequestStartingWithAdjectiveComparative() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        // greener, adjective comparative (JJR)
        StructuredUserStory sus = createWithRequest("greener fuel to be sold in our stations");
        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Group of stakeholders User indicated in one story that they want greener fuel to be sold in their stations.")));
    }

    @Test
    public void generateCorrectTextGivenARequestStartingWithAdjectiveSuperlative() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        // most, adjective superlative (JJS)
        StructuredUserStory sus = createWithRequest("most of the products to be green");
        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Group of stakeholders User indicated in one story that they want most of the products to be green.")));
    }

    @Test
    public void generateCorrectTextGivenARequestStartingWithAdverb() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        // frequently, adverb (RB)
        StructuredUserStory sus = createWithRequest("frequently buy products near my house");
        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Group of stakeholders User indicated in one story that they want frequently buy products near their house.")));
    }

    @Test
    public void generateCorrectTextGivenARequestStartingWithAdverbComparative() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        // faster, adverb comparative (RBR)
        StructuredUserStory sus = createWithRequest("faster to finish my work");
        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Group of stakeholders User indicated in one story that they want faster to finish their work.")));
    }

    // TODO: Test the other grammar classes

    // TODO: Test with more than 1 story
    @Test
    public void generateCorrectTextGiven2StoriesForTheSameUser() {
        RequestGenerator requestGenerator = new RequestGenerator(posModel, detokenizerDict);

        String rewrittenSentence = requestGenerator.generate(DEFAULT_USER,
                Arrays.asList(createWithRequest("to purchase products from selected suppliers"),
                        createWithRequest("to repeat a past purchase")));

        String expected = "Group of stakeholders User indicated in 2 stories that they want to purchase products from selected suppliers and repeat a past purchase.";
        assertThat(rewrittenSentence, is(equalTo(expected)));
    }

}
