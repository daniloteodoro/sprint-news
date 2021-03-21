package com.sprintnews.domain.model.textgenerator;

import com.sprintnews.domain.model.StructuredUserStory;
import com.sprintnews.domain.model.utils.TextUtils;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.DetokenizationDictionary;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RuleBuilder;

import static com.sprintnews.domain.model.utils.TextUtils.isBlank;

import java.util.*;
import java.util.stream.Collectors;

public class RequestGenerator {
    private static final String POS_TAG_PERIOD = ".";
    private static final String POS_TAG_COMMA = ",";
    private static final String FACT_TAG = "tags";
    private static final String FACT_TOKEN = "tokens";
    private static final String FACT_SENTENCE = "sentence";

    private final Rules rulesForRequests;
    private final RulesEngine engine;
    private final SimpleTokenizer tokenizer;
    private final DictionaryDetokenizer englishDetokenizer;
    private final POSTaggerME posTagger;

    public RequestGenerator(POSModel model, DetokenizationDictionary detokenizerDictionary) {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.englishDetokenizer = new DictionaryDetokenizer(detokenizerDictionary);
        this.posTagger = new POSTaggerME(model);

        // TODO: Reuse this rule (request/benefit generators)
        Rule changeSentenceTo3rdPersonRule = new RuleBuilder()
                .name("Change Sentence to Third Person Rule")
                .when(facts -> {
                    String[] tokens = facts.get(FACT_TOKEN);

                    return Arrays.stream(tokens)
                            .anyMatch(token -> token.equalsIgnoreCase("I") ||
                                    token.equalsIgnoreCase("my") ||
                                    token.equalsIgnoreCase("our"));
                })
                .then(facts -> {
                    String[] tokens = facts.get(FACT_TOKEN);

                    for (int i = 0; i < tokens.length; i++) {
                        String currToken = tokens[i];
                        if (currToken.equalsIgnoreCase("I"))
                            tokens[i] = "they";
                        else if (currToken.equalsIgnoreCase("my"))
                            tokens[i] = "their";
                        else if (currToken.equalsIgnoreCase("our"))
                            tokens[i] = "their";
                    }

                    // Update sentence and tags with token changes
                    String sentence = this.englishDetokenizer.detokenize(tokens, null);
                    String[] tags = posTagger.tag(tokens);

                    facts.put(FACT_TAG, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        Rule removePeriod = new RuleBuilder()
                .name("Remove period from sentence Rule")
                .when(facts -> {
                    String[] tags = facts.get(FACT_TAG);
                    if (tags.length == 0)
                        return false;

                    // Symbols like ?! or ... are also represented by the tag "."
                    return POS_TAG_PERIOD.equals(tags[tags.length - 1]) ||
                            POS_TAG_COMMA.equals(tags[tags.length - 1]);
                })
                .then(facts -> {
                    String[] tokens = facts.get(FACT_TOKEN);
                    String[] tags = facts.get(FACT_TAG);

                    tags = Arrays.copyOf(tags, tags.length - 1);
                    tokens = Arrays.copyOf(tokens, tokens.length - 1);

                    String sentence = this.englishDetokenizer.detokenize(tokens, null);
                    // Update sentence and tags with token changes
                    facts.put(FACT_TAG, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        Rule addToBeforeAVerbRule = new RuleBuilder()
                .name("Add 'to' Before A Verb Rule")
                .when(facts -> {
                    String[] tags = facts.get(FACT_TAG);
                    if (tags.length == 0)
                        return false;

                    return PartOfSpeechTag.parse(tags[0])
                            .filter(PartOfSpeechTag::isVerbBaseForm)
                            .isPresent();
                })
                .then(facts -> {
                    String sentence = "to ".concat(facts.get(FACT_SENTENCE));

                    String[] tokens = tokenizer.tokenize(sentence);
                    String[] tags = posTagger.tag(tokens);

                    // Update sentence and tags with token changes
                    facts.put(FACT_TAG, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        this.rulesForRequests = new Rules();
        this.rulesForRequests.register(addToBeforeAVerbRule, changeSentenceTo3rdPersonRule, removePeriod);

        this.engine = new DefaultRulesEngine();
    }

    public String generate(String stakeholder, List<StructuredUserStory> stories) {
        if (isBlank(stakeholder) || stories == null || stories.isEmpty())
            return "";

        boolean singleStory = stories.size() == 1;

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
                    facts.put(FACT_TAG, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);

                    engine.fire(rulesForRequests, facts);

                    return facts.get(FACT_SENTENCE);
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
