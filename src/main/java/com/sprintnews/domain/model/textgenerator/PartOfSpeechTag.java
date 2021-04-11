package com.sprintnews.domain.model.textgenerator;

import java.util.Optional;

/**
 * List of part-of-speech tags used in the Penn Treebank Project:
 * https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
 */
public enum PartOfSpeechTag {
    CC("Coordinating conjunction"),
    CD("Cardinal number"),
    DT("Determiner"),
    EX("Existential there"),
    FW("Foreign word"),
    IN("Preposition or subordinating conjunction"),
    JJ("Adjective"),
    JJR("Adjective, comparative"),
    JJS("Adjective, superlative"),
    LS("List item marker"),         // 10
    MD("Modal"),
    NN("Noun, singular or mass"),   // 12
    NNS("Noun, plural"),
    NNP("Proper noun, singular"),   // 14
    NNPS("Proper noun, plural"),
    NP("Proper noun, singular"),    // ?
    PDT("Predeterminer"),
    POS("Possessive ending"),
    PRP("Personal pronoun"),
    PRP$("Possessive pronoun"),
    RB("Adverb"),                   // 20
    RBR("Adverb, comparative"),
    RBS("Adverb, superlative"),
    RP("Particle"),                 // 23
    SYM("Symbol"),
    TO("to"),
    UH("Interjection"),
    VB("Verb, base form"),          // 27
    VBD("Verb, past tense"),
    VBG("Verb, gerund or present participle"),      // 29
    VBN("Verb, past participle"),
    VBP("Verb, non-3rd person singular present"),
    VBZ("Verb, 3rd person singular present"),       // 32
    WDT("Wh-determiner"),
    WP("Wh-pronoun"),
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
