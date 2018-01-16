package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.verification.rule.state.DefaultRuleStateProvider;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

/**
 * Provides a limited set of active rules for internal verification. It is missing rules for document existence and content
 * validation. This allows the policy to be used on Envelopes that have detached documents represented by
 * {@link com.guardtime.envelope.document.EmptyDocument}
 */
public class LimitedInternalVerificationPolicy extends AbstractVerificationPolicy {

    public LimitedInternalVerificationPolicy() {
        RuleStateProvider stateProvider = new DefaultRuleStateProvider();
        signatureContentRules.addAll(CommonPolicyRuleSets.getManifestRules(stateProvider));
        signatureContentRules.addAll(CommonPolicyRuleSets.getAnnotationRules(stateProvider));
    }

}
