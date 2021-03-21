package com.sprintnews.domain.model.textgenerator;

import org.jeasy.rules.api.Action;
import org.jeasy.rules.api.Facts;

// TODO: Review / remove
public class ConnectingSentenceAction implements Action {

    @Override
    public void execute(Facts facts) throws Exception {
        String sentence = facts.get("sentence");
        facts.put("sentence", "can be sure that ".concat(sentence));
    }

    public static ConnectingSentenceAction addConnectingSentence() {
        return new ConnectingSentenceAction();
    }

}
