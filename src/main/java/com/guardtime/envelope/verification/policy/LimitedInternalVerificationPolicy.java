package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.signature.SignatureVerifier;
import com.guardtime.envelope.verification.rule.state.RuleState;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_MANIFEST_HASH;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_SIGNATURE_EXISTS;

/**
 * Provides a limited set of active rules for internal verification. It is missing rules for document existence and content
 * validation. This allows the policy to be used on Envelopes that have detached documents represented by
 * {@link com.guardtime.envelope.document.EmptyDocument}
 */
public class LimitedInternalVerificationPolicy extends DefaultVerificationPolicy {

    public LimitedInternalVerificationPolicy() {
        this(
                new LimitedInternalRuleStateProvider(),
                new InternalSignatureVerifier(),
                Collections.<Rule<Envelope>>emptyList(),
                Collections.<Rule<SignatureContent>>emptyList()
        );
    }

    public LimitedInternalVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier,
                                             List<Rule<Envelope>> envelopeRules, List<Rule<SignatureContent>> signatureRules) {
        super(stateProvider, signatureVerifier, envelopeRules, signatureRules);


    }


    protected static class LimitedInternalRuleStateProvider implements RuleStateProvider {
        protected final List<String> allowedRules = new ArrayList<>();

        public LimitedInternalRuleStateProvider() {
            allowedRules.add(KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName());
            allowedRules.add(KSIE_VERIFY_DATA_MANIFEST.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION_MANIFEST.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION_EXISTS.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName());
            allowedRules.add(KSIE_VERIFY_ANNOTATION_DATA.getName());
            allowedRules.add(KSIE_VERIFY_SIGNATURE_EXISTS.getName());
            allowedRules.add(KSIE_VERIFY_MANIFEST_HASH.getName());
        }

        @Override
        public RuleState getStateForRule(String name) {
            return allowedRules.contains(name) ? RuleState.FAIL : RuleState.IGNORE;
        }
    }

    /**
     * Ignores signature verification
     */
    protected static class InternalSignatureVerifier implements SignatureVerifier {
        @Override
        public Boolean isSupported(EnvelopeSignature envelopeSignature) {
            return true;
        }

        @Override
        public SignatureResult getSignatureVerificationResult(final Object signature, Manifest manifest) {
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
