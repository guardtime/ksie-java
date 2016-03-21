package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.policy.rule.ContainerRule;
import com.guardtime.container.verification.policy.rule.SignatureContentRule;
import com.guardtime.container.verification.policy.rule.generic.*;

import java.util.LinkedList;
import java.util.List;

public class RecommendedVerificationPolicy implements VerificationPolicy {

    @Override
    public List<ContainerRule> getGeneralRules() {
        List<ContainerRule> rules = new LinkedList<>();
        // skip MimeTypeRule as this will probably be package specific
        rules.add(new ManifestConsecutivityRule());
        return rules;
    }

    @Override
    public List<SignatureContentRule> getSignatureContentRules() {
        List<SignatureContentRule> rules = new LinkedList<>();
        // skip signature verification at the moment as this requires knowledge of signature type and its verification possibility
        rules.add(new DataFilesManifestIntegrityRule());
        rules.add(new DataFileIntegrityRule());
        rules.add(new AnnotationsManifestIntegrityRule());
        rules.add(new AnnotationIntegrityRule());
        return rules;
    }
}
