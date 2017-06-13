/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.verification;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Helper class to verify {@link Container} based on a {@link VerificationPolicy}
 */
public class ContainerVerifier {
    private static final Logger logger = LoggerFactory.getLogger(ContainerVerifier.class);

    private VerificationPolicy policy;

    public ContainerVerifier(VerificationPolicy policy) {
        Util.notNull(policy, "Verification policy");
        this.policy = policy;
    }

    /**
     * Verifies the {@link Container} based on the rules provided by the {@link VerificationPolicy}.
     * @param container  container to be verified
     * @return {@link VerifiedContainer} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public VerifiedContainer verify(Container container) {
        ResultHolder holder = new ResultHolder();
        try {
            verifyGeneralRules(container, holder);
            verifySignatureContents(container.getSignatureContents(), holder);
        } catch (RuleTerminatingException e) {
            logger.info("Container verification terminated! Reason: '{}'", e.getMessage());
        }
        return new VerifiedContainer(container, holder);
    }

    private void verifyGeneralRules(Container container, ResultHolder holder) throws RuleTerminatingException {
        for (Rule<Container> rule : policy.getContainerRules()) {
            rule.verify(holder, container);
        }
    }

    private void verifySignatureContents(List<? extends SignatureContent> signatureContents, ResultHolder holder) {
        for (SignatureContent content : signatureContents) {
            try {
                for (Rule<SignatureContent> rule : policy.getSignatureContentRules()) {
                    rule.verify(holder, content);
                }
            } catch (RuleTerminatingException e) {
                logger.info("Signature content verification terminated! Reason: '{}'", e.getMessage());
            }
        }
    }

}
