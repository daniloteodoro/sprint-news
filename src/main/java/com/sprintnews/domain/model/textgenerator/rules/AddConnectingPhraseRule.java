package com.sprintnews.domain.model.textgenerator.rules;

import com.sprintnews.domain.model.textgenerator.PartOfSpeechTag;
import com.sprintnews.domain.model.utils.TextUtils;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Rule
public class AddConnectingPhraseRule {
    private final SimpleTokenizer tokenizer;
    private final POSTaggerME posTagger;
    private final List<String> connectingSentences;

    public AddConnectingPhraseRule(SimpleTokenizer tokenizer, POSTaggerME posTagger, List<String> connectingPhrases) {
        this.tokenizer = Objects.requireNonNull(tokenizer);
        this.posTagger = Objects.requireNonNull(posTagger);
        this.connectingSentences = Objects.requireNonNull(connectingPhrases);
    }

    @Condition
    public boolean checkCondition(@Fact(FactKeys.FACT_TAG) String[] tags) {
        Optional<PartOfSpeechTag> firstTag = PartOfSpeechTag.parse(tags[0]);
        // Always add the connecting sentence for now (more tests needed)
        return firstTag.filter(partOfSpeechTag -> true
                            /*partOfSpeechTag == PartOfSpeechTag.JJ ||
                                    partOfSpeechTag == PartOfSpeechTag.JJR ||
                                    partOfSpeechTag == PartOfSpeechTag.JJS*/).isPresent();
    }

    @Action
    public void addConnectingPhrase(Facts facts, @Fact(FactKeys.FACT_ISFIRSTSTORY) Boolean isFirstStory, @Fact(FactKeys.FACT_SENTENCE) String sentence) {
        String connectedSentence = isFirstStory ? TextUtils.getNextRandomItemFrom(connectingSentences).concat(sentence) : " that ".concat(sentence);

        String[] tokens = tokenizer.tokenize(connectedSentence);
        String[] tags = posTagger.tag(tokens);

        // Update sentence and tags with token changes
        facts.put(FactKeys.FACT_TAG, tags);
        facts.put(FactKeys.FACT_TOKEN, tokens);
        facts.put(FactKeys.FACT_SENTENCE, connectedSentence);
    }

}
