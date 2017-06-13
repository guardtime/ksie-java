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

package com.guardtime.container.verification.rule.signature;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;

/**
 * Provides signature specific verification for Container verification process.
 * @param <S>    Signature type that can be verified
 */
public interface SignatureVerifier<S> {

    Boolean isSupported(ContainerSignature containerSignature);

    SignatureResult getSignatureVerificationResult(S signature, Manifest manifest) throws RuleTerminatingException;

}
