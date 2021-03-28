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
public class ChangeSentenceToThirdPersonRule {
    private final DictionaryDetokenizer englishDetokenizer;
    private final POSTaggerME posTagger;

    public ChangeSentenceToThirdPersonRule(DictionaryDetokenizer englishDetokenizer, POSTaggerME posTagger) {
        this.englishDetokenizer = Objects.requireNonNull(englishDetokenizer);
        this.posTagger = Objects.requireNonNull(posTagger);
    }

    @Condition
    public boolean checkCondition(@Fact(FactKeys.FACT_TOKEN) String[] tokens) {
        return Arrays.stream(tokens)
                .anyMatch(token -> token.equalsIgnoreCase("I") ||
                        token.equalsIgnoreCase("my") ||
                        token.equalsIgnoreCase("our") ||
                        token.equalsIgnoreCase("am"));
    }

    @Action
    public void changeTo3rdPerson(org.jeasy.rules.api.Facts facts, @Fact(FactKeys.FACT_TOKEN) String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            String currToken = tokens[i];
            if (currToken.equalsIgnoreCase("I"))
                tokens[i] = "they";
            else if (currToken.equalsIgnoreCase("my"))
                tokens[i] = "their";
            else if (currToken.equalsIgnoreCase("our"))
                tokens[i] = "their";
            else if (currToken.equalsIgnoreCase("am"))
                tokens[i] = "are";
        }

        // Update sentence and tags with token changes
        String sentence = this.englishDetokenizer.detokenize(tokens, null);
        String[] tags = posTagger.tag(tokens);

        facts.put(FactKeys.FACT_TAG, tags);
        facts.put(FactKeys.FACT_TOKEN, tokens);
        facts.put(FactKeys.FACT_SENTENCE, sentence);
    }

}
