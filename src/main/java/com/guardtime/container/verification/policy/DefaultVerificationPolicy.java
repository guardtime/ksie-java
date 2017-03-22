package com.guardtime.container.verification.policy;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.AnnotationDataExistenceRule;
import com.guardtime.container.verification.rule.generic.AnnotationDataIntegrityRule;
import com.guardtime.container.verification.rule.generic.AnnotationsManifestExistenceRule;
import com.guardtime.container.verification.rule.generic.AnnotationsManifestIntegrityRule;
import com.guardtime.container.verification.rule.generic.DocumentExistenceRule;
import com.guardtime.container.verification.rule.generic.DocumentIntegrityRule;
import com.guardtime.container.verification.rule.generic.DocumentsManifestExistenceRule;
import com.guardtime.container.verification.rule.generic.DocumentsManifestIntegrityRule;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.SignatureExistenceRule;
import com.guardtime.container.verification.rule.generic.SignatureIntegrityRule;
import com.guardtime.container.verification.rule.generic.SignatureSignsManifestRule;
import com.guardtime.container.verification.rule.generic.SingleAnnotationManifestExistenceRule;
import com.guardtime.container.verification.rule.generic.SingleAnnotationManifestIntegrityRule;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link VerificationPolicy}
 * Contains rules for:
 * <ol>
 *   <li>verifying MIME type</li>
 *   <li>verifying signature</li>
 *   <li>verifying {@link DocumentsManifest}</li>
 *   <li>verifying {@link com.guardtime.container.document.ContainerDocument}s</li>
 *   <li>verifying {@link com.guardtime.container.manifest.AnnotationsManifest}</li>
 *   <li>verifying {@link com.guardtime.container.manifest.SingleAnnotationManifest}s</li>
 *   <li>verifying {@link com.guardtime.container.annotation.ContainerAnnotation}s</li>
 * </ol>
 * May contain extra rules to add specialized verification requirements to the policy or to overwrite some of the
 * pre-existing rules.
 */
public class DefaultVerificationPolicy implements VerificationPolicy {
    private ArrayList<Rule> rules = new ArrayList<>();

    /**
     * @param signatureVerifier will be called for verifying each signature.
     * @param packagingFactory will be used to create the appropriate MIME type rule.
     */
    public DefaultVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier, ContainerPackagingFactory packagingFactory) {
        this(stateProvider, signatureVerifier, packagingFactory, Collections.<Rule>emptyList());
    }

    public DefaultVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier, ContainerPackagingFactory packagingFactory, List<Rule> customRules) {
        rules.add(new MimeTypeIntegrityRule(stateProvider, packagingFactory));
        rules.add(new SignatureExistenceRule(stateProvider));
        rules.add(new SignatureSignsManifestRule(stateProvider));
        rules.add(new SignatureIntegrityRule(stateProvider, signatureVerifier));
        rules.add(new DocumentsManifestExistenceRule(stateProvider));
        rules.add(new DocumentsManifestIntegrityRule(stateProvider));
        rules.add(new DocumentExistenceRule(stateProvider));
        rules.add(new DocumentIntegrityRule(stateProvider));
        rules.add(new AnnotationsManifestExistenceRule(stateProvider));
        rules.add(new AnnotationsManifestIntegrityRule(stateProvider));
        rules.add(new SingleAnnotationManifestExistenceRule(stateProvider));
        rules.add(new SingleAnnotationManifestIntegrityRule(stateProvider));
        rules.add(new AnnotationDataExistenceRule(stateProvider));
        rules.add(new AnnotationDataIntegrityRule(stateProvider));
        rules.addAll(customRules);
    }

    public List<Rule> getRules() {
        return rules;
    }

}
