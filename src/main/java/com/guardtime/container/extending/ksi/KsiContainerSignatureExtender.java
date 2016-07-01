package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.ContainerSignatureExtender;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

/**
 * Extends all {@link KSISignature}s in {@link com.guardtime.container.packaging.Container} to the "closest" publication
 * in publication file available to provided {@link KSI}.
 */
public class KsiContainerSignatureExtender extends ContainerSignatureExtender<KSISignature> {
    protected final KSI ksi;

    public KsiContainerSignatureExtender(KSI ksi) {
        Util.notNull(ksi, "KSI");
        this.ksi = ksi;
    }

    @Override
    protected boolean unsupportedContainerSignature(ContainerSignature containerSignature) {
        return !(containerSignature.getSignature() instanceof KSISignature);
    }

    @Override
    protected KSISignature getExtendedSignature(ContainerSignature containerSignature) throws SignatureException {
        try {
            KSISignature ksiSignature = (KSISignature) containerSignature.getSignature();
            return ksi.extend(ksiSignature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }
}
