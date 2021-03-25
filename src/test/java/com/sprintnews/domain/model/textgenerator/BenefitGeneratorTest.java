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
 * Main goal here is to check how the Benefit Generator behaves for each specific part of speech.
 * ps: Some of the tests have and therefore produce grammatically strange results in order to test some part of speeches.
 * @author daniloteodoro
 */
public class BenefitGeneratorTest {
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

    private StructuredUserStory createWithBenefit(String benefit) {
        return StructuredUserStory.parse("key", "title", new String[]{String.format("As a User, I want to do something, so that %s.", benefit)});
    }

    @Test
    public void generateCorrectTextGivenABenefitStartingWithAdjective() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        // green, adjective (JJ)
        StructuredUserStory sus = createWithBenefit("green becomes the main color");
        String rewrittenSentence = benefitGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("User can be sure that green becomes the main color with story key-title.")));
    }

    @Test
    public void generateCorrectTextGivenABenefitStartingWithAdjectiveComparative() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        // greener, adjective comparative (JJR)
        StructuredUserStory sus = createWithBenefit("greener products become our main income");
        String rewrittenSentence = benefitGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("User can be sure that greener products become their main income with story key-title.")));
    }

    @Test
    public void generateCorrectTextGivenABenefitStartingWithAdjectiveSuperlative() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        // most, adjective superlative (JJS)
        StructuredUserStory sus = createWithBenefit("most of our income comes from green products");
        String rewrittenSentence = benefitGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("User can be sure that most of their income comes from green products with story key-title.")));
    }

    @Test
    public void generateCorrectTextGivenABenefitStartingWithAdverb() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        // frequently, adverb (RB)
        StructuredUserStory sus = createWithBenefit("almost all the time I find what I want quickly");
        String rewrittenSentence = benefitGenerator.generate(DEFAULT_USER, Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("User can be sure that almost all the time they find what they want quickly with story key-title.")));
    }

    @Test
    public void generateCorrectTextGivenABenefitStartingWithAdverbComparative() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        // faster, adverb comparative (RBR)
        StructuredUserStory sus = createWithBenefit("more effectively I am");
        String rewrittenSentence = benefitGenerator.generate("Yoda", Collections.singletonList(sus));

        assertThat(rewrittenSentence, is(equalTo("Yoda can be sure that more effectively they are with story key-title.")));
    }

    // TODO: Test the other grammar classes

    // TODO: Test with more than 1 story
    // Testing a second story with the same user.
    @Test
    public void generateCorrectTextGiven2StoriesForTheSameUser() {
        BenefitGenerator benefitGenerator = new BenefitGenerator(posModel, detokenizerDict);

        String rewrittenSentence = benefitGenerator.generate("Buyer",
                Arrays.asList(createWithBenefit("a product never runs out of stock"), createWithBenefit("I speed up my work")));

        String expected = "Buyer can be sure that a product never runs out of stock with story key-title and that they speed up their work with story key-title.";
        assertThat(rewrittenSentence, is(equalTo(expected)));
    }

}
