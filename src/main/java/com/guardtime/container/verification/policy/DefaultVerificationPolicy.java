package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.*;

import java.util.LinkedList;
import java.util.List;

public class DefaultVerificationPolicy implements VerificationPolicy {
    private List<Rule> rules = new LinkedList<>();

    public DefaultVerificationPolicy(List<Rule> extraRules) {
        rules.add(new ManifestConsecutivityRule());
        rules.add(new DataFilesManifestIntegrityRule());
        rules.add(new DataFileIntegrityRule());
        rules.add(new AnnotationsManifestIntegrityRule());
        rules.add(new AnnotationIntegrityRule());
        rules.addAll(extraRules);
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

}
