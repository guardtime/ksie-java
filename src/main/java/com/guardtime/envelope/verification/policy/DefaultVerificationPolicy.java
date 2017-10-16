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

package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.packaging.Envelope;
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
import com.guardtime.envelope.verification.rule.state.DefaultRuleStateProvider;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

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
 *   <li>verifying {@link Document}s</li>
 *   <li>verifying {@link com.guardtime.envelope.manifest.AnnotationsManifest}</li>
 *   <li>verifying {@link com.guardtime.envelope.manifest.SingleAnnotationManifest}s</li>
 *   <li>verifying {@link Annotation}s</li>
 * </ol>
 * May contain extra rules to add specialized verification requirements to the policy or to overwrite some of the
 * pre-existing rules.
 */
public class DefaultVerificationPolicy implements VerificationPolicy {
    private ArrayList<Rule<SignatureContent>> signatureContentRules = new ArrayList<>();
    private ArrayList<Rule<Envelope>> envelopeRules = new ArrayList<>();

    /**
     * @param signatureVerifier will be called for verifying each signature.
     *
     */
    public DefaultVerificationPolicy(SignatureVerifier signatureVerifier) {
        this(
                new DefaultRuleStateProvider(),
                signatureVerifier,
                Collections.<Rule<Envelope>>emptyList(),
                Collections.<Rule<SignatureContent>>emptyList()
        );
    }

    public DefaultVerificationPolicy(RuleStateProvider stateProvider, SignatureVerifier signatureVerifier,
                                     List<Rule<Envelope>> customEnvelopeRules,
                                     List<Rule<SignatureContent>> customSignatureContentRules) {
        envelopeRules.addAll(customEnvelopeRules);
        signatureContentRules.add(new SignatureExistenceRule(stateProvider));
        signatureContentRules.add(new SignatureSignsManifestRule(stateProvider));
        signatureContentRules.add(new SignatureIntegrityRule(stateProvider, signatureVerifier));
        signatureContentRules.add(new DocumentsManifestExistenceRule(stateProvider));
        signatureContentRules.add(new DocumentsManifestIntegrityRule(stateProvider));
        signatureContentRules.add(new DocumentExistenceRule(stateProvider));
        signatureContentRules.add(new DocumentIntegrityRule(stateProvider));
        signatureContentRules.add(new AnnotationsManifestExistenceRule(stateProvider));
        signatureContentRules.add(new AnnotationsManifestIntegrityRule(stateProvider));
        signatureContentRules.add(new SingleAnnotationManifestExistenceRule(stateProvider));
        signatureContentRules.add(new SingleAnnotationManifestIntegrityRule(stateProvider));
        signatureContentRules.add(new AnnotationDataExistenceRule(stateProvider));
        signatureContentRules.add(new AnnotationDataIntegrityRule(stateProvider));
        signatureContentRules.addAll(customSignatureContentRules);
    }

    public List<Rule<SignatureContent>> getSignatureContentRules() {
        return signatureContentRules;
    }

    @Override
    public List<Rule<Envelope>> getEnvelopeRules() {
        return envelopeRules;
    }

}
