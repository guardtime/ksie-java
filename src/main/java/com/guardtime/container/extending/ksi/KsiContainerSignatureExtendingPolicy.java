package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

/**
 * Extends all {@link KSISignature}s in {@link com.guardtime.container.packaging.Container} to the "closest" publication
 * in publication file available to provided {@link KSI}.
 */
public class KsiContainerSignatureExtendingPolicy implements ExtendingPolicy<KSISignature> {
    protected final KSI ksi;

    public KsiContainerSignatureExtendingPolicy(KSI ksi) {
        Util.notNull(ksi, "KSI");
        this.ksi = ksi;
    }

    public KSISignature getExtendedSignature(KSISignature ksiSignature) throws SignatureException {
        try {
            return ksi.extend(ksiSignature);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

}
