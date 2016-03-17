package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.InputStream;

public class KsiSignatureFactory implements SignatureFactory {

    private static final KsiSignatureFactoryType SIGNATURE_FACTORY_TYPE = new KsiSignatureFactoryType();

    private final KSI ksi;

    public KsiSignatureFactory(KSI ksi) {
        Util.notNull(ksi, "KSI");
        this.ksi = ksi;
    }

    @Override
    public KsiContainerSignature create(DataHash hash) throws SignatureException {
        Util.notNull(ksi, "DataHash");
        try {
            KSISignature signature = ksi.sign(hash);
            return new KsiContainerSignature(signature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public KsiContainerSignature read(InputStream input) throws SignatureException {
        Util.notNull(ksi, "Input stream");
        try {
            KSISignature signature = ksi.read(input);
            return new KsiContainerSignature(signature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public SignatureFactoryType getSignatureFactoryType() {
        return SIGNATURE_FACTORY_TYPE;
    }

}
