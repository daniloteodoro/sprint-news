package com.sprintnews.domain.model.textgenerator;

import java.util.Optional;

/**
 * List of part-of-speech tags used in the Penn Treebank Project
 */
public enum PartOfSpeechTag {
    DT("Determiner"),
    IN("Preposition or subordinating conjunction"),
    JJ("Adjective"),
    JJR("Adjective, comparative"),
    JJS("Adjective, superlative"),
    MD("Modal"),
    NN("Noun, singular or mass"),
    NNS("Noun, plural"),
    NP("Proper noun, singular"),
    NNPS("Proper noun, plural"),
    PDT("Predeterminer"),
    POS("Possessive ending"),
    PRP("Personal pronoun"),
    PRP$("Possessive pronoun"),
    RB("Adverb"),
    RBR("Adverb, comparative"),
    RBS("Adverb, superlative"),
    RP("Particle"),
    TO("to"),
    VB("Verb, base form"),
    VBD("Verb, past tense"),
    VBG("Verb, gerund or present participle"),
    VBN("Verb, past participle"),
    VBP("Verb, non-3rd person singular present"),
    VBZ("Verb, 3rd person singular present"),
    WP$("Possessive wh-pronoun"),
    WRB("Wh-adverb");

    private final String tagDescription;

    PartOfSpeechTag(String tagDescription) {
        this.tagDescription = tagDescription;
    }

    public static Optional<PartOfSpeechTag> parse(String abbreviation) {
        try {
            return Optional.of(valueOf(abbreviation));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isVerbBaseForm() {
        return this == VB;
    }

    public boolean isTo() {
        return this == TO;
    }

    public String getTagDescription() {
        return tagDescription;
    }
}
