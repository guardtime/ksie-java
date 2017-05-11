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

package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;

public class MultiHashElementIntegrityRule extends AbstractRule<Pair<MultiHashElement, FileReference>> {

    private final UnimplementedHashAlgorithmExistenceRule unimplementedHashAlgorithmExistenceRule;
    private final TrustedHashAlgorithmExistenceRule trustedHashAlgorithmExistenceRule;
    private final TrustedHashListIntegrityRule trustedHashesIntegrityRule;

    protected MultiHashElementIntegrityRule(RuleState state, String name) {
        super(state);

        unimplementedHashAlgorithmExistenceRule = new UnimplementedHashAlgorithmExistenceRule(state, name);
        trustedHashAlgorithmExistenceRule = new TrustedHashAlgorithmExistenceRule(state, name);
        trustedHashesIntegrityRule = new TrustedHashListIntegrityRule(state, name);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<MultiHashElement, FileReference> verifiable) throws RuleTerminatingException {
        unimplementedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashesIntegrityRule.verify(holder, verifiable);
    }
}
