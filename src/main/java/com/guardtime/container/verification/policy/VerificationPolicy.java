package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.policy.rule.VerificationRule;

import java.util.List;

public interface VerificationPolicy {

    /**
     * Contains all rules to be performed on container to prove validity.
     * As an example should contains rules for:
     * <ol>
     *   <li>verifying mimetype</li>
     *   <li>verifying manifest indexes are consecutive</li>
     *   <li>verifying signature</li>
     *   <li>verifying data manifest</li>
     *   <li>verifying data files</li>
     *   <li>verifying annotations manifest</li>
     *   <li>verifying annotations (including annotation manifests)</li>
     * </ol>
     * May contain extra rules to add specialized verification requirements to the policy.
     * @return List of all rules to be performed when verifying with the policy.
     */
    List<VerificationRule> getRules();

}