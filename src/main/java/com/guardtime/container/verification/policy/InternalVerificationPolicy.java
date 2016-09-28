package com.guardtime.container.verification.policy;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;

import java.util.Arrays;
import java.util.List;

import static com.guardtime.container.verification.rule.RuleType.*;

public class InternalVerificationPolicy extends DefaultVerificationPolicy {

    /**
     * @param packagingFactory    will be used to create the appropriate MIME type rule.
     */
    public InternalVerificationPolicy(ContainerPackagingFactory packagingFactory) {
        super(new InternalRuleStateProvider(), new InternalSignatureVerifier(), packagingFactory);
    }

    private static class InternalRuleStateProvider implements RuleStateProvider {
        private final List<String> allowedRules = Arrays.asList(
                KSIE_FORMAT.getName(),
                KSIE_VERIFY_MANIFEST_INDEX.getName(),
                KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName(),
                KSIE_VERIFY_DATA_MANIFEST.getName(),
                KSIE_VERIFY_DATA_EXISTS.getName(),
                KSIE_VERIFY_DATA_HASH.getName(),
                KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION_MANIFEST.getName(),
                KSIE_VERIFY_ANNOTATION_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION.getName(),
                KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName(),
                KSIE_VERIFY_ANNOTATION_DATA.getName(),
                KSIE_VERIFY_SIGNATURE_EXISTS.getName()
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
        public VerificationResult getSignatureVerificationResult(Object signature, Manifest manifest) throws RuleTerminatingException {
            return VerificationResult.OK;
        }
    }
}
