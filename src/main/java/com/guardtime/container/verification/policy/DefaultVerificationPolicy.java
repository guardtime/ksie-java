package com.guardtime.container.verification.policy;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleStateProvider;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.SignatureContentIntegrityRule;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of {@link VerificationPolicy}
 * Contains containerRules for:
 * <ol>
 *   <li>verifying MIME type</li>
 *   <li>verifying signature</li>
 *   <li>verifying {@link DocumentsManifest}</li>
 *   <li>verifying {@link com.guardtime.container.document.ContainerDocument}s</li>
 *   <li>verifying {@link com.guardtime.container.manifest.AnnotationsManifest}</li>
 *   <li>verifying {@link com.guardtime.container.manifest.SingleAnnotationManifest}s</li>
 *   <li>verifying {@link com.guardtime.container.annotation.ContainerAnnotation}s</li>
 * </ol>
 * May contain extra containerRules to add specialized verification requirements to the policy or to overwrite some of the
 * pre-existing containerRules.
 */
public class DefaultVerificationPolicy implements VerificationPolicy {
    private ArrayList<ContainerRule> containerRules = new ArrayList<>();

    /**
     * @param signatureVerifier will be called for verifying each signature.
     * @param packagingFactory will be used to create the appropriate MIME type rule.
     */
    public DefaultVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier, ContainerPackagingFactory packagingFactory) {
        this(stateProvider, signatureVerifier, packagingFactory, new LinkedList<ContainerRule>());
    }

    public DefaultVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier, ContainerPackagingFactory packagingFactory, List<ContainerRule> customRules) {
        containerRules.add(new MimeTypeIntegrityRule(stateProvider, packagingFactory));
        containerRules.add(new SignatureContentIntegrityRule(stateProvider, signatureVerifier)); // Nested rules inside
        containerRules.addAll(customRules);
    }

    public List<ContainerRule> getContainerRules() {
        return containerRules;
    }

}
