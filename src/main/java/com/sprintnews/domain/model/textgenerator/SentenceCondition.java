package com.sprintnews.domain.model.textgenerator;

import com.sprintnews.domain.model.exception.SprintNewsError;
import org.jeasy.rules.api.Condition;
import org.jeasy.rules.api.Facts;

// TODO: Check / remove
public class SentenceCondition implements Condition {
    private final PartOfSpeechTag tag;

    public SentenceCondition(PartOfSpeechTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean evaluate(Facts facts) {
        String sentence = facts.get("sentence");
        switch (tag) {
            case JJ: return sentence != null && !sentence.isEmpty();
            case JJR:
            case JJS:
                return false;
            default: throw new SprintNewsError("Unhandled PoS tag: " + tag.getTagDescription());
        }
    }

    public static SentenceCondition startsWithAdjective() {
        return new SentenceCondition(PartOfSpeechTag.JJ);
    }
    public static SentenceCondition startsWithAdjectiveComparative() {
        return new SentenceCondition(PartOfSpeechTag.JJR);
    }
    public static SentenceCondition startsWithAdjectiveSuperlative() {
        return new SentenceCondition(PartOfSpeechTag.JJS);
    }

}
