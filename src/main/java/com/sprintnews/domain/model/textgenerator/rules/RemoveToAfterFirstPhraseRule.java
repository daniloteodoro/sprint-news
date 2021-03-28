package com.sprintnews.domain.model.textgenerator.rules;

import com.sprintnews.domain.model.textgenerator.PartOfSpeechTag;
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
public class RemoveToAfterFirstPhraseRule {

    private final DictionaryDetokenizer englishDetokenizer;
    private final POSTaggerME posTagger;

    public RemoveToAfterFirstPhraseRule(DictionaryDetokenizer englishDetokenizer, POSTaggerME posTagger) {
        this.englishDetokenizer = Objects.requireNonNull(englishDetokenizer);
        this.posTagger = Objects.requireNonNull(posTagger);
    }

    @Condition
    public boolean checkCondition(@Fact(FactKeys.FACT_TAG) String[] tags, @Fact(FactKeys.FACT_SENTENCE_NR) Integer sentenceNr) {
        if (sentenceNr <= 1)
            return false;

        if (tags.length == 0)
            return false;

        return PartOfSpeechTag.parse(tags[0])
                .filter(PartOfSpeechTag::isTo)
                .isPresent();
    }

    @Action
    public void removeToFromTheBeginning(org.jeasy.rules.api.Facts facts, @Fact(FactKeys.FACT_TOKEN) String[] tokens, @Fact(FactKeys.FACT_TAG) String[] tags) {
        tags = Arrays.copyOfRange(tags, 1, tags.length);
        tokens = Arrays.copyOfRange(tokens, 1, tokens.length);

        String sentence = this.englishDetokenizer.detokenize(tokens, null);

        // Update sentence and tags with token changes
        facts.put(FactKeys.FACT_TAG, tags);
        facts.put(FactKeys.FACT_TOKEN, tokens);
        facts.put(FactKeys.FACT_SENTENCE, sentence);
    }

}
