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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.sprintnews.domain.model.utils.TextUtils.isBlank;

public class BenefitGenerator {
    private static final String POS_TAG_PERIOD = ".";
    private static final String POS_TAG_COMMA = ",";
    private static final String FACT_TAGS = "tags";
    private static final String FACT_TOKEN = "tokens";
    private static final String FACT_SENTENCE = "sentence";
    private static final String FACT_ISFIRSTSTORY = "isFirstStory";

    private final Rules rulesForBenefits;
    private final RulesEngine engine;
    private final SimpleTokenizer tokenizer;
    private final DictionaryDetokenizer englishDetokenizer;
    private final POSTaggerME posTagger;
    private final List<String> connectingSentences;

    public BenefitGenerator(POSModel model, DetokenizationDictionary detokenizerDictionary, List<String> connectingSentences) {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.englishDetokenizer = new DictionaryDetokenizer(detokenizerDictionary);
        this.posTagger = new POSTaggerME(model);
        this.connectingSentences = connectingSentences != null ? connectingSentences : Collections.singletonList(" can be sure that ");

        Rule connectSentenceRule = new RuleBuilder()
                .name("Connect Sentence Rule")
                .when(facts -> {
                    String[] tags = facts.get(FACT_TAGS);

                    Optional<PartOfSpeechTag> firstTag = PartOfSpeechTag.parse(tags[0]);
                    return firstTag.filter(partOfSpeechTag -> true
                            /*partOfSpeechTag == PartOfSpeechTag.JJ ||
                                    partOfSpeechTag == PartOfSpeechTag.JJR ||
                                    partOfSpeechTag == PartOfSpeechTag.JJS*/).isPresent();
                })
                .then(facts -> {
                    Boolean isFirstStory = facts.get(FACT_ISFIRSTSTORY);
                    String sentence = isFirstStory ? TextUtils.getNextRandomItemFrom(connectingSentences).concat(facts.get(FACT_SENTENCE)) : " that ".concat(facts.get(FACT_SENTENCE));

                    String[] tokens = tokenizer.tokenize(sentence);
                    String[] tags = posTagger.tag(tokens);
                    // Update sentence and tags with token changes
                    facts.put(FACT_TAGS, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        Rule changeSentenceTo3rdPersonRule = new RuleBuilder()
                .name("Change Sentence to Third Person Rule")
                .when(facts -> {
                    String[] tokens = facts.get(FACT_TOKEN);

                    return Arrays.stream(tokens)
                            .anyMatch(token -> token.equalsIgnoreCase("I") ||
                                    token.equalsIgnoreCase("my") ||
                                    token.equalsIgnoreCase("our") ||
                                    token.equalsIgnoreCase("am"));
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
                        else if (currToken.equalsIgnoreCase("am"))
                            tokens[i] = "are";
                    }

                    // Update sentence and tags with token changes
                    String sentence = this.englishDetokenizer.detokenize(tokens, null);
                    String[] tags = posTagger.tag(tokens);

                    facts.put(FACT_TAGS, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        Rule removePeriod = new RuleBuilder()
                .name("Remove period from sentence Rule")
                .when(facts -> {
                    String[] tags = facts.get("tags");
                    if (tags.length == 0)
                        return false;

                    // Symbols like ?! or ... are also represented by the tag "."
                    return POS_TAG_PERIOD.equals(tags[tags.length - 1]);
                })
                .then(facts -> {
                    String[] tokens = facts.get(FACT_TOKEN);
                    String[] tags = facts.get(FACT_TAGS);

                    tags = Arrays.copyOf(tags, tags.length - 1);
                    tokens = Arrays.copyOf(tokens, tokens.length - 1);

                    String sentence = this.englishDetokenizer.detokenize(tokens, null);
                    // Update sentence and tags with token changes
                    facts.put(FACT_TAGS, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                })
                .build();

        this.rulesForBenefits = new Rules();
        this.rulesForBenefits.register(changeSentenceTo3rdPersonRule, connectSentenceRule, removePeriod);

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
                    facts.put(FACT_TAGS, tags);
                    facts.put(FACT_TOKEN, tokens);
                    facts.put(FACT_SENTENCE, sentence);
                    facts.put(FACT_ISFIRSTSTORY, isFirstStory.get());

                    engine.fire(rulesForBenefits, facts);

                    sentence = facts.get(FACT_SENTENCE);
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
