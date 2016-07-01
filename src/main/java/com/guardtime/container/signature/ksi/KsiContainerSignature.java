package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ContainerSignature} implementation with {@link KSISignature} as the underlying signature.
 */
class KsiContainerSignature implements ContainerSignature<KSISignature> {

    private KSISignature signature;

    public KsiContainerSignature(KSISignature signature) {
        this.signature = signature;
    }

    public KSISignature getSignature() {
        return signature;
    }

    @Override
    public void extend(KSISignature extendedSignature) throws SignatureException {
        if (extendedSignature != null &&
                isExtendedOriginal(extendedSignature)) {
            this.signature = extendedSignature;
        }
        throw new SignatureException("Invalid extended signature!");
    }

    /**
     * Returns true when the passed in signature is extended and has the same input hash and signing date-time as
     * this.signature.
     * @param extendedSignature to be compared with this.signature.
     */
    private boolean isExtendedOriginal(KSISignature extendedSignature) {
        return extendedSignature.getInputHash().equals(signature.getInputHash()) &&
                extendedSignature.isExtended() &&
                extendedSignature.getAggregationTime().equals(signature.getAggregationTime()) &&
                extendedSignature.getIdentity().equals(signature.getIdentity());
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try {
            signature.writeTo(output);
        } catch (KSIException e) {
            throw new IOException("Writing signature to output failed", e);
        }
    }

}
