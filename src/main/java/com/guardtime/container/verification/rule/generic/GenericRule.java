package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class GenericRule implements Rule {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);
    protected final RuleState state;
    protected final String name;

    public GenericRule(String name) {
        this(RuleState.FAIL, name);
    }

    public GenericRule(RuleState state, String name) {
        this.state = state;
        this.name = name;
    }

    protected RuleResult getFailureResult() {
        return RuleState.WARN.equals(state) ? RuleResult.WARN : RuleResult.NOK;
    }

    @Override
    public String getName() {
        return name;
    }

    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> asReturnablePairList(Object obj, RuleVerificationResult result) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> returnable = new LinkedList<>();
        returnable.add(Pair.of(obj, result));
        return returnable;
    }
}
