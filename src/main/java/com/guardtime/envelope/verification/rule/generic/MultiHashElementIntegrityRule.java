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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.state.RuleState;

/**
 * Sub rule that provides {@link com.guardtime.ksi.hashing.DataHash} validation for other rules which may need it.
 */
public class MultiHashElementIntegrityRule extends AbstractRule<Pair<EnvelopeElement, FileReference>> {

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
    protected void verifyRule(ResultHolder holder, Pair<EnvelopeElement, FileReference> verifiable) throws RuleTerminatingException {
        unimplementedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashesIntegrityRule.verify(holder, verifiable);
    }
}
