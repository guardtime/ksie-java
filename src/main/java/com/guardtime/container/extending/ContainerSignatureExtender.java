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

package com.guardtime.container.extending;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extending for all signatures in a container.
 */
public class ContainerSignatureExtender {
    private static final Logger logger = LoggerFactory.getLogger(ContainerSignatureExtender.class);
    private final SignatureFactory signatureFactory;
    private final ExtendingPolicy policy;

    public ContainerSignatureExtender(SignatureFactory signatureFactory, ExtendingPolicy policy) {
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(policy, "Extending policy");
        this.signatureFactory = signatureFactory;
        this.policy = policy;
    }

    /**
     * Extends each signature in input container and returns an {@link ExtendedContainer}.
     * If a signature extending fails it is logged at INFO level and skipped.
     * @param container    Container to be extended.
     */
    public ExtendedContainer extend(Container container) {
        for (SignatureContent content : container.getSignatureContents()) {
            Manifest manifest = content.getManifest().getRight();
            String signatureUri = manifest.getSignatureReference().getUri();
            try {
                ContainerSignature containerSignature = content.getContainerSignature();
                signatureFactory.extend(containerSignature, policy);

                if (!containerSignature.isExtended()) {
                    logger.warn("Extending signature '{}' resulted in a non-extended signature without exception!", signatureUri);
                }
            } catch (SignatureException e) {
                logger.warn("Failed to extend signature '{}' because: '{}'", signatureUri, e.getMessage());
            }
        }
        return new ExtendedContainer(container);
    }

}
