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

package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation for a {@link SignatureFactory} that allows for postponing real signing.
 * Requires actual {@link SignatureFactory} for providing proper signatures once they are available.
 * For creating placeholder {@link EnvelopeSignature}s a {@link SignatureFactoryType} or {@link SignatureFactory} must be
 * provided to indicate what type of signatures will eventually be used.
 */
public class PostponedSignatureFactory implements SignatureFactory {

    private final SignatureFactoryType factoryType;
    private final SignatureFactory signatureFactory;

    public PostponedSignatureFactory(SignatureFactoryType factoryType) {
        this.factoryType = factoryType;
        this.signatureFactory = null;
    }

    public PostponedSignatureFactory(SignatureFactory factory) {
        if (factory.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Provided SignatureFactory may not be " + this.getClass().getSimpleName());
        }
        this.factoryType = factory.getSignatureFactoryType();
        this.signatureFactory = factory;
    }

    @Override
    public EnvelopeSignature create(DataHash hash) {
        return new PostponedSignature<Object>(hash);
    }

    @Override
    public EnvelopeSignature read(InputStream input) throws SignatureException {
        try {
            return new PostponedSignature<Object>(new DataHash(Util.toByteArray(input)));
        } catch (IllegalArgumentException | IOException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public void extend(EnvelopeSignature envelopeSignature, ExtendingPolicy extender) throws SignatureException {
        if (signatureFactory == null) {
            throw new IllegalStateException("Not supported if SignatureFactory is not provided to constructor!");
        }
        signatureFactory.extend(envelopeSignature, extender);
    }

    @Override
    public SignatureFactoryType getSignatureFactoryType() {
        return factoryType;
    }

    /**
     * Replaces placeholder underlying signature in {@link EnvelopeSignature} for provided {@param signatureContent}.
     *
     * @throws SignatureException       - When trying to replace placeholder signature which has already been filled.
     * @throws IllegalStateException    - When no {@link SignatureFactory} is provided to constructor
     * @throws IllegalArgumentException - When trying to replace placeholder signature with signature that has non-matching
     * {@link DataHash} OR provided {@link SignatureContent} does not hold {@link EnvelopeSignature} with type of
     * {@link PostponedSignature}.
     */
    public void sign(SignatureContent signatureContent) throws SignatureException {
        if (signatureFactory == null) {
            throw new IllegalStateException("Not supported if SignatureFactory is not provided to constructor!");
        }
        if (!(signatureContent.getEnvelopeSignature() instanceof PostponedSignature)) {
            throw new IllegalArgumentException(
                    "Provided SignatureContent does not hold PostponedSignature type of EnvelopeSignature"
            );
        }
        PostponedSignature postponedSignature = (PostponedSignature) signatureContent.getEnvelopeSignature();
        EnvelopeSignature signature = signatureFactory.create(postponedSignature.getSignedDataHash());
        postponedSignature.sign(signature);
    }
}
