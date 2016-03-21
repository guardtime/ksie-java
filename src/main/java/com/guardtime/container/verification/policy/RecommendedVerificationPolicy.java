package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.SignatureContentRule;
import com.guardtime.container.verification.rule.generic.*;

import java.util.LinkedList;
import java.util.List;

public class RecommendedVerificationPolicy implements VerificationPolicy {
    private List<ContainerRule> generalRules = new LinkedList<>();
    private List<SignatureContentRule> contentRules = new LinkedList<>();

    private RecommendedVerificationPolicy(ContainerRule mimeTypeRule, SignatureContentRule signatureRule) {
        generalRules.add(mimeTypeRule);
        generalRules.add(new ManifestConsecutivityRule());

        contentRules.add(signatureRule);
        contentRules.add(new DataFilesManifestIntegrityRule());
        contentRules.add(new DataFileIntegrityRule());
        contentRules.add(new AnnotationsManifestIntegrityRule());
        contentRules.add(new AnnotationIntegrityRule());
    }

    @Override
    public List<ContainerRule> getGeneralRules() {
        return generalRules;
    }

    @Override
    public List<SignatureContentRule> getSignatureContentRules() {
        return contentRules;
    }

    public static class Builder {
        private ContainerRule mimeTypeRule;
        private SignatureContentRule signatureRule;

        public Builder withMimeTypeRule(ContainerRule rule) {
            this.mimeTypeRule = rule;
            return this;
        }

        public Builder withSignatureRule(SignatureContentRule rule) {
            this.signatureRule = rule;
            return this;
        }

        public RecommendedVerificationPolicy build() {
            return new RecommendedVerificationPolicy(mimeTypeRule, signatureRule);
        }
    }
}
