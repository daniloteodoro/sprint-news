package com.sprintnews.domain.model.textgenerator;

import com.sprintnews.domain.model.StructuredUserStory;
import com.sprintnews.domain.model.textgenerator.rules.ChangeSentenceToThirdPersonRule;
import com.sprintnews.domain.model.textgenerator.rules.FactKeys;
import com.sprintnews.domain.model.textgenerator.rules.RemovePeriodFromSentenceRule;
import com.sprintnews.domain.model.textgenerator.rules.RemoveToAfterFirstPhraseRule;
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

import static com.sprintnews.domain.model.utils.TextUtils.isBlank;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RequestGenerator {
    private final Rules rulesForRequests;
    private final RulesEngine engine;
    private final SimpleTokenizer tokenizer;
    private final POSTaggerME posTagger;

    public RequestGenerator(POSModel model, DetokenizationDictionary detokenizerDictionary) {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.posTagger = new POSTaggerME(model);
        DictionaryDetokenizer englishDetokenizer = new DictionaryDetokenizer(detokenizerDictionary);

        ChangeSentenceToThirdPersonRule changeSentenceTo3rdPersonRule = new ChangeSentenceToThirdPersonRule(englishDetokenizer, posTagger);
        RemovePeriodFromSentenceRule removePeriodRule = new RemovePeriodFromSentenceRule(englishDetokenizer, posTagger);
        RemoveToAfterFirstPhraseRule removeToAfterFirstPhraseRule = new RemoveToAfterFirstPhraseRule(englishDetokenizer, posTagger);

        this.rulesForRequests = new Rules();
        this.rulesForRequests.register(removeToAfterFirstPhraseRule, changeSentenceTo3rdPersonRule, removePeriodRule);

        this.engine = new DefaultRulesEngine();
    }

    public String generate(String stakeholder, List<StructuredUserStory> stories) {
        if (isBlank(stakeholder) || stories == null || stories.isEmpty())
            return "";

        boolean singleStory = stories.size() == 1;
        AtomicInteger sentenceNr = new AtomicInteger(0);

        List<String> sentences = stories.stream()
                .map(story -> {
                    if (isBlank(story.getRequest()))
                        return "";

                    String sentence = story.getRequest().trim();
                    String[] tokens = tokenizer.tokenize(sentence);
                    String[] tags = posTagger.tag(tokens);

                    if (tags.length == 0)
                        return "";

                    Facts facts = new Facts();
                    facts.put(FactKeys.FACT_TAG, tags);
                    facts.put(FactKeys.FACT_TOKEN, tokens);
                    facts.put(FactKeys.FACT_SENTENCE, sentence);
                    facts.put(FactKeys.FACT_SENTENCE_NR, sentenceNr.incrementAndGet());

                    engine.fire(rulesForRequests, facts);

                    return facts.get(FactKeys.FACT_SENTENCE);
                })
                .filter(TextUtils::isNotBlank)
                .collect(Collectors.toList());

        if (sentences.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Group of stakeholders ")
                .append(stakeholder)
                .append(" indicated in ")
                .append(singleStory ? "one" : stories.size())
                .append(singleStory ? " story" : " stories")
                .append(" that they want ")
                .append(TextUtils.generateHumanReadableList(sentences, String::toString))
                .append(".");

        return sb.toString();
    }
}
