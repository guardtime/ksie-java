package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

public class PostponedSignatureFactory implements SignatureFactory {

    private final SignatureFactoryType realType;

    public PostponedSignatureFactory(SignatureFactoryType realType) {
        this.realType = realType;
    }

    @Override
    public EnvelopeSignature create(DataHash hash) throws SignatureException {
        return null;
    }

    @Override
    public EnvelopeSignature read(InputStream input) throws SignatureException {
        return new PostponedSignature();
    }

    @Override
    public void extend(EnvelopeSignature envelopeSignature, ExtendingPolicy extender) throws SignatureException {

    }

    @Override
    public SignatureFactoryType getSignatureFactoryType() {
        return realType;
    }

}
