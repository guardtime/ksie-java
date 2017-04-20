package com.guardtime.container.verification.policy;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.rule.Rule;

import java.util.List;

/**
 * Access interface for providing {@link Rule}s to be used for verifying a {@link com.guardtime.container.packaging.Container}
 * Contains rules to be performed on container to prove validity.
 * As an example should contain rules for:
 * <ol>
 *   <li>verifying MIME-type</li>
 *   <li>verifying signature</li>
 *   <li>verifying data manifest</li>
 *   <li>verifying data files</li>
 *   <li>verifying annotations manifest</li>
 *   <li>verifying annotations (including annotation manifests)</li>
 * </ol>
 * May contain extra rules to add specialized verification requirements to the policy.
 */
public interface VerificationPolicy {

    List<Rule<SignatureContent>> getSignatureContentRules();

    List<Rule<Container>> getContainerRules();
}