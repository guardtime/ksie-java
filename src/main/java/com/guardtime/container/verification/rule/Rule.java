package com.guardtime.container.verification.rule;

public interface Rule {

    RuleState getState();

    String getName();
}
