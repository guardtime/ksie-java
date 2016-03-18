package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.policy.rule.generic.*;

import java.util.LinkedList;
import java.util.List;

public class RecommendedVerificationPolicy implements VerificationPolicy {
    private List<VerificationRule> rules = new LinkedList<>();

    @Override
    public List<VerificationRule> getRules() {
        // skip MimeTypeRule as this will probably be package specific
        rules.add(new ManifestConsecutivityRule());
        // skip signature verification at the moment as this requires knowledge of signature type and its verification possibility
        rules.add(new DataFilesManifestIntegrityRule());
        rules.add(new DataFileIntegrityRule());
        rules.add(new AnnotationsManifestIntegrityRule());
        rules.add(new AnnotationIntegrityRule());
        return rules;
    }
}
