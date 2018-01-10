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

// TODO: Javadoc
public class PostponedSignatureFactory implements SignatureFactory {

    private final SignatureFactoryType realType;
    private final SignatureFactory realFactory;

    public PostponedSignatureFactory(SignatureFactoryType realType) {
        this.realType = realType;
        this.realFactory = null;
    }

    public PostponedSignatureFactory(SignatureFactory realFactory) {
        this.realType = realFactory.getSignatureFactoryType();
        this.realFactory = realFactory;
    }

    @Override
    public EnvelopeSignature create(DataHash hash) throws SignatureException {
        return new PostponedSignature(hash);
    }

    @Override
    public EnvelopeSignature read(InputStream input) throws SignatureException {
        try {
            return new PostponedSignature(new DataHash(Util.toByteArray(input)));
        } catch (IOException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public void extend(EnvelopeSignature envelopeSignature, ExtendingPolicy extender) throws SignatureException {
        if(realFactory == null) {
            throw new UnsupportedOperationException("Not supported if SignatureFactory is not provided to constructor!");
        }
        realFactory.extend(envelopeSignature, extender);
    }

    @Override
    public SignatureFactoryType getSignatureFactoryType() {
        return realType;
    }

    public void sign(SignatureContent content) throws SignatureException {
        if(realFactory == null) {
            throw new UnsupportedOperationException("Not supported if SignatureFactory is not provided to constructor!");
        }
        PostponedSignature postponedSignature = (PostponedSignature) content.getEnvelopeSignature();
        EnvelopeSignature signature = realFactory.create(postponedSignature.getSignedDataHash());
        postponedSignature.sign(signature);
    }

}
