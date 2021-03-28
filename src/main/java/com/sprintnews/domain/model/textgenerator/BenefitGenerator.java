package com.sprintnews.domain.model.textgenerator;

import com.sprintnews.domain.model.StructuredUserStory;
import com.sprintnews.domain.model.textgenerator.rules.AddConnectingPhraseRule;
import com.sprintnews.domain.model.textgenerator.rules.ChangeSentenceToThirdPersonRule;
import com.sprintnews.domain.model.textgenerator.rules.FactKeys;
import com.sprintnews.domain.model.textgenerator.rules.RemovePeriodFromSentenceRule;
import com.sprintnews.domain.model.utils.TextUtils;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.DetokenizationDictionary;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.sprintnews.domain.model.utils.TextUtils.isBlank;

public class BenefitGenerator {
    private final Rules rulesForBenefits;
    private final RulesEngine engine;
    private final SimpleTokenizer tokenizer;
    private final POSTaggerME posTagger;

    public BenefitGenerator(POSModel model, DetokenizationDictionary detokenizerDictionary, List<String> connectingPhraseList) {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.posTagger = new POSTaggerME(model);
        DictionaryDetokenizer englishDetokenizer = new DictionaryDetokenizer(detokenizerDictionary);
        List<String> connectingPhrase = Objects.requireNonNull(connectingPhraseList);

        AddConnectingPhraseRule connectSentenceRule = new AddConnectingPhraseRule(tokenizer, posTagger, connectingPhrase);
        ChangeSentenceToThirdPersonRule changeSentenceTo3rdPersonRule = new ChangeSentenceToThirdPersonRule(englishDetokenizer, posTagger);
        RemovePeriodFromSentenceRule removePeriodRule = new RemovePeriodFromSentenceRule(englishDetokenizer, posTagger);

        this.rulesForBenefits = new Rules();
        this.rulesForBenefits.register(changeSentenceTo3rdPersonRule, connectSentenceRule, removePeriodRule);

        this.engine = new DefaultRulesEngine();
    }

    public String generate(String stakeholder, List<StructuredUserStory> stories) {
        if (isBlank(stakeholder) || stories == null || stories.isEmpty())
            return "";

        AtomicReference<Boolean> isFirstStory = new AtomicReference<>(true);

        List<String> sentences = stories.stream()
                .map(story -> {
                    if (isBlank(story.getBenefit()))
                        return "";

                    String sentence = story.getBenefit().trim();
                    String[] tokens = tokenizer.tokenize(sentence);
                    String[] tags = posTagger.tag(tokens);

                    if (tags.length == 0)
                        return "";

                    Facts facts = new Facts();
                    facts.put(FactKeys.FACT_TAG, tags);
                    facts.put(FactKeys.FACT_TOKEN, tokens);
                    facts.put(FactKeys.FACT_SENTENCE, sentence);
                    facts.put(FactKeys.FACT_ISFIRSTSTORY, isFirstStory.get());

                    engine.fire(rulesForBenefits, facts);

                    sentence = facts.get(FactKeys.FACT_SENTENCE);
                    if (isBlank(sentence))
                        return "";

                    isFirstStory.set(false);

                    return sentence.concat(" with story ")
                            .concat(story.getStoryIdAndTitle());
                })
                .filter(TextUtils::isNotBlank)
                .collect(Collectors.toList());

        if (sentences.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(stakeholder)
                .append(" ")
                .append(TextUtils.generateHumanReadableList(sentences, String::toString))
                .append(".");

        return sb.toString();
    }
}
