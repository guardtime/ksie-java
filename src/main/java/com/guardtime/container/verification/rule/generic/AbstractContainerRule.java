package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleState;

public abstract class AbstractContainerRule extends AbstractRuleHelper<Container> implements ContainerRule {

    protected AbstractContainerRule(RuleState state) {
        super(state);
    }

}
