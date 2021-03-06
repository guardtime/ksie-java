/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.rule.Rule;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractVerificationPolicy implements VerificationPolicy {
    protected ArrayList<Rule<SignatureContent>> signatureContentRules = new ArrayList<>();
    protected ArrayList<Rule<Envelope>> envelopeRules = new ArrayList<>();

    public List<Rule<SignatureContent>> getSignatureContentRules() {
        return signatureContentRules;
    }

    public List<Rule<Envelope>> getEnvelopeRules() {
        return envelopeRules;
    }
}
