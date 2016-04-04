package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

public class KsiSignatureExtender implements SignatureExtender {
    private final KSI ksi;

    public KsiSignatureExtender(KSI ksi) {
        this.ksi = ksi;
    }

    @Override
    public ContainerSignature extend(ContainerSignature signature) throws SignatureException {
        try {
            KSISignature extendableSignature = ((KsiContainerSignature) signature).getSignature();
            return new KsiContainerSignature(ksi.extend(extendableSignature));
        } catch (ClassCastException | KSIException e) {
            throw new SignatureException(e);
        }
    }
}
