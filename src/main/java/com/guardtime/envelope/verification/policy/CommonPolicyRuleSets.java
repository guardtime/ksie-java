package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.generic.AnnotationDataExistenceRule;
import com.guardtime.envelope.verification.rule.generic.AnnotationDataIntegrityRule;
import com.guardtime.envelope.verification.rule.generic.AnnotationsManifestExistenceRule;
import com.guardtime.envelope.verification.rule.generic.AnnotationsManifestIntegrityRule;
import com.guardtime.envelope.verification.rule.generic.DocumentExistenceRule;
import com.guardtime.envelope.verification.rule.generic.DocumentIntegrityRule;
import com.guardtime.envelope.verification.rule.generic.DocumentsManifestExistenceRule;
import com.guardtime.envelope.verification.rule.generic.DocumentsManifestIntegrityRule;
import com.guardtime.envelope.verification.rule.generic.SignatureExistenceRule;
import com.guardtime.envelope.verification.rule.generic.SignatureIntegrityRule;
import com.guardtime.envelope.verification.rule.generic.SignatureSignsManifestRule;
import com.guardtime.envelope.verification.rule.generic.SingleAnnotationManifestExistenceRule;
import com.guardtime.envelope.verification.rule.generic.SingleAnnotationManifestIntegrityRule;
import com.guardtime.envelope.verification.rule.signature.SignatureVerifier;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper that provides pre-compiled lists of commonly used rules.
 */
public abstract class CommonPolicyRuleSets {

    /**
     * Returns a list of all rules related to underlying signature verification.
     */
    public static List<Rule<SignatureContent>> getSignatureRules(RuleStateProvider provider, SignatureVerifier verifier) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.addAll(getBasicSignatureRules(provider));
        rules.add(new SignatureIntegrityRule(provider, verifier));
        return rules;
    }

    /**
     * Returns a list of basic rules to verify existence of underlying signature and its association to manifest.
     */
    public static List<Rule<SignatureContent>> getBasicSignatureRules(RuleStateProvider provider) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.add(new SignatureExistenceRule(provider));
        rules.add(new SignatureSignsManifestRule(provider));
        return rules;
    }

    /**
     * Returns rules associated with document existence and integrity.
     */
    public static List<Rule<SignatureContent>> getDocumentRules(RuleStateProvider provider) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.add(new DocumentExistenceRule(provider));
        rules.add(new DocumentIntegrityRule(provider));
        return rules;
    }

    /**
     * Returns rules associated with annotation existence and integrity. Including per annotation manifest related rules.
     */
    public static List<Rule<SignatureContent>> getAnnotationRules(RuleStateProvider provider) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.add(new SingleAnnotationManifestExistenceRule(provider));
        rules.add(new SingleAnnotationManifestIntegrityRule(provider));
        rules.add(new AnnotationDataExistenceRule(provider));
        rules.add(new AnnotationDataIntegrityRule(provider));
        return rules;
    }

    /**
     * Returns rules associated with documents and annotations manifests of {@link com.guardtime.envelope.packaging.Envelope}.
     */
    public static List<Rule<SignatureContent>> getManifestRules(RuleStateProvider provider) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.add(new DocumentsManifestExistenceRule(provider));
        rules.add(new DocumentsManifestIntegrityRule(provider));
        rules.add(new AnnotationsManifestExistenceRule(provider));
        rules.add(new AnnotationsManifestIntegrityRule(provider));
        return rules;
    }

    /**
     * Returns rules necessary for verifying internal integrity of Envelope {@link com.guardtime.envelope.packaging.Envelope}.
     */
    public static List<Rule<SignatureContent>> getIntegrityRules(RuleStateProvider provider) {
        List<Rule<SignatureContent>> rules = new ArrayList<>();
        rules.addAll(getBasicSignatureRules(provider));
        rules.addAll(getManifestRules(provider));
        rules.addAll(getAnnotationRules(provider));
        rules.addAll(getDocumentRules(provider));
        return rules;
    }

}
