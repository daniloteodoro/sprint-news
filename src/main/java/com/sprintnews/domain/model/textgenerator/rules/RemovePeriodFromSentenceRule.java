package com.sprintnews.domain.model.textgenerator.rules;

import com.sprintnews.domain.model.textgenerator.rules.FactKeys;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.Arrays;
import java.util.Objects;

@Rule
public class RemovePeriodFromSentenceRule {
    private static final String POS_TAG_PERIOD = ".";
    private static final String POS_TAG_COMMA = ",";

    private final DictionaryDetokenizer englishDetokenizer;
    private final POSTaggerME posTagger;

    public RemovePeriodFromSentenceRule(DictionaryDetokenizer englishDetokenizer, POSTaggerME posTagger) {
        this.englishDetokenizer = Objects.requireNonNull(englishDetokenizer);
        this.posTagger = Objects.requireNonNull(posTagger);
    }

    @Condition
    public boolean checkForPeriod(@Fact(FactKeys.FACT_TAG) String[] tags) {
        if (tags.length == 0)
            return false;

        // Symbols like ?! or ... are also represented by the tag "."
        return POS_TAG_PERIOD.equals(tags[tags.length - 1]) ||
                POS_TAG_COMMA.equals(tags[tags.length - 1]);
    }

    @Action
    public void removePeriodFromSentence(org.jeasy.rules.api.Facts facts, @Fact(FactKeys.FACT_TOKEN) String[] tokens, @Fact(FactKeys.FACT_TAG) String[] tags) {
        tags = Arrays.copyOf(tags, tags.length - 1);
        tokens = Arrays.copyOf(tokens, tokens.length - 1);

        String sentence = this.englishDetokenizer.detokenize(tokens, null);

        // Update sentence and tags with token changes
        facts.put(FactKeys.FACT_TAG, tags);
        facts.put(FactKeys.FACT_TOKEN, tokens);
        facts.put(FactKeys.FACT_SENTENCE, sentence);
    }

}
