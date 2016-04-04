package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

public class KsiSignatureExtender implements SignatureExtender {
    protected final KSI ksi;

    public KsiSignatureExtender(KSI ksi) {
        Util.notNull(ksi, "KSI");
        this.ksi = ksi;
    }

    @Override
    public ContainerSignature extend(ContainerSignature signature) throws SignatureException {
        try {
            KSISignature extendableSignature = ((KsiContainerSignature) signature).getSignature();
            return getExtendedSignature(extendableSignature);
        } catch (ClassCastException | KSIException e) {
            throw new SignatureException(e);
        }
    }

    protected KsiContainerSignature getExtendedSignature(KSISignature extendableSignature) throws KSIException {
        return new KsiContainerSignature(ksi.extend(extendableSignature));
    }
}
