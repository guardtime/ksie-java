package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

/**
 * SignatureExtender for {@link KSISignature} based ContainerSignatures. Uses logic for extending to closest publication
 * to signature.
 */
public class KsiSignatureExtender implements SignatureExtender {
    protected final KSI ksi;

    public KsiSignatureExtender(KSI ksi) {
        Util.notNull(ksi, "KSI");
        this.ksi = ksi;
    }

    @Override
    public ContainerSignature extend(ContainerSignature containerSignature) throws SignatureException {
        if(!containerSignature.supportsSignatureClass(KSISignature.class)) {
            throw new SignatureException("Unsupported ContainerSignature provided for extending.");
        }
        try {
            KSISignature extendableSignature = (KSISignature) containerSignature.getSignature();
            return getExtendedSignature(extendableSignature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

    protected KsiContainerSignature getExtendedSignature(KSISignature extendableSignature) throws KSIException {
        return new KsiContainerSignature(ksi.extend(extendableSignature));
    }
}
