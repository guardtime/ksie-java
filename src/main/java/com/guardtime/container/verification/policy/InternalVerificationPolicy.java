package com.guardtime.container.verification.policy;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.KSIE_FORMAT;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_HASH;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_MANIFEST_HASH;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_SIGNATURE_EXISTS;

public class InternalVerificationPolicy extends DefaultVerificationPolicy {

    /**
     * @param packagingFactory    will be used to create the appropriate MIME type rule.
     */
    public InternalVerificationPolicy(ContainerPackagingFactory packagingFactory) {
        super(
                new InternalRuleStateProvider(),
                new InternalSignatureVerifier(),
                packagingFactory,
                Collections.<Rule<Container>>emptyList(),
                Collections.<Rule<SignatureContent>>emptyList()
        );
    }

    private static class InternalRuleStateProvider implements RuleStateProvider {
        private final List<String> allowedRules = Arrays.asList(
                KSIE_FORMAT.getName(),
                KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName(),
                KSIE_VERIFY_DATA_MANIFEST.getName(),
                KSIE_VERIFY_DATA_HASH.getName(),
                KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION_MANIFEST.getName(),
                KSIE_VERIFY_ANNOTATION_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION.getName(),
                KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION_DATA.getName(),
                KSIE_VERIFY_SIGNATURE_EXISTS.getName(),
                KSIE_VERIFY_MANIFEST_HASH.getName()
        );

        @Override
        public RuleState getStateForRule(String name) {
            return allowedRules.contains(name) ? RuleState.FAIL : RuleState.IGNORE;
        }
    }

    /**
     * Ignores signature verification
     */
    private static class InternalSignatureVerifier implements SignatureVerifier {
        @Override
        public Boolean isSupported(ContainerSignature containerSignature) {
            return true;
        }

        @Override
        public SignatureResult getSignatureVerificationResult(final Object signature, Manifest manifest) throws RuleTerminatingException {
            return new SignatureResult() {
                @Override
                public VerificationResult getSimplifiedResult() {
                    return VerificationResult.OK;
                }

                @Override
                public Object getSignature() {
                    return signature;
                }

                @Override
                public Object getFullResult() {
                    return null;
                }
            };
        }
    }
}
