package com.guardtime.container.verification.policy;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.ManifestIndexConsistencyRule;
import com.guardtime.container.verification.rule.generic.SignatureContentIntegrityRule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of {@link VerificationPolicy}
 * Contains containerRules for:
 * <ol>
 *   <li>verifying MIME type</li>
 *   <li>verifying manifest indexes are consecutive</li>
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
     * @param signatureRule will be called for verifying each signature.
     * @param mimetypeRule will be called to verify mimetype file.
     */
    public DefaultVerificationPolicy(Rule signatureRule, ContainerRule mimetypeRule) {
        this(signatureRule, mimetypeRule, new LinkedList<ContainerRule>());
    }

    public DefaultVerificationPolicy(Rule signatureRule, ContainerRule mimetypeRule, List<ContainerRule> customRules) {
        containerRules.add(mimetypeRule);
        containerRules.add(new ManifestIndexConsistencyRule());
        containerRules.add(new SignatureContentIntegrityRule(signatureRule)); // Nested rules inside
        containerRules.addAll(customRules);
    }

    public List<ContainerRule> getContainerRules() {
        return containerRules;
    }

}
