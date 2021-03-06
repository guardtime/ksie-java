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

package com.guardtime.envelope.signature.ksi;

import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.Reader;
import com.guardtime.ksi.Signer;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.InputStream;

/**
 * Implementation of the {@link SignatureFactory} that uses KSI {@link Signer} and {@link Reader}
 * for the underlying signature handling logic.
 */
public class KsiSignatureFactory implements SignatureFactory {

    private static final KsiSignatureFactoryType SIGNATURE_FACTORY_TYPE = new KsiSignatureFactoryType();

    private final Signer signer;
    private final Reader reader;

    public KsiSignatureFactory(Signer signer, Reader reader) {
        this.signer = signer;
        this.reader = reader;
    }

    @Override
    public KsiEnvelopeSignature create(DataHash hash) throws SignatureException {
        Util.notNull(signer, "Signer");
        Util.notNull(hash, "DataHash");
        try {
            KSISignature signature = signer.sign(hash);
            return new KsiEnvelopeSignature(signature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public KsiEnvelopeSignature read(InputStream input) throws SignatureException {
        Util.notNull(reader, "Reader");
        Util.notNull(input, "Input stream");
        try {
            KSISignature signature = reader.read(input);
            return new KsiEnvelopeSignature(signature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public void extend(EnvelopeSignature envelopeSignature, ExtendingPolicy extender) throws SignatureException {
        if (unsupportedEnvelopeSignature(envelopeSignature)) {
            throw new SignatureException("Unsupported EnvelopeSignature provided for extending.");
        }
        KsiEnvelopeSignature ksiEnvelopeSignature = (KsiEnvelopeSignature) envelopeSignature;
        KSISignature extendedSignature = (KSISignature) extender.getExtendedSignature(ksiEnvelopeSignature.getSignature());
        ksiEnvelopeSignature.setExtendedSignature(extendedSignature);
    }

    private boolean unsupportedEnvelopeSignature(EnvelopeSignature envelopeSignature) {
        return !(envelopeSignature instanceof KsiEnvelopeSignature);
    }

    @Override
    public SignatureFactoryType getSignatureFactoryType() {
        return SIGNATURE_FACTORY_TYPE;
    }

}
