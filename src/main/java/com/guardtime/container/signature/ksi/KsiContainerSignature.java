package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ContainerSignature} implementation with {@link KSISignature} as the underlying signature.
 */
class KsiContainerSignature implements ContainerSignature<KSISignature> {

    private KSISignature signature;

    KsiContainerSignature(KSISignature signature) {
        this.signature = signature;
    }

    @Override
    public KSISignature getSignature() {
        return signature;
    }

    @Override
    public DataHash getSignedDataHash() {
        return signature.getInputHash();
    }

    @Override
    public boolean isExtended() {
        return signature.isExtended();
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try {
            signature.writeTo(output);
        } catch (KSIException e) {
            throw new IOException("Writing signature to output failed", e);
        }
    }

    void setExtendedSignature(KSISignature newSignature) throws SignatureException {
        if (!newSignature.isExtended() ||
                !newSignature.getInputHash().equals(this.signature.getInputHash()) ||
                !newSignature.getAggregationTime().equals(this.signature.getAggregationTime()) ||
                !newSignature.getIdentity().equals(this.signature.getIdentity())
                ) {
            throw new SignatureException("Provided signature is not an extended variant of the existing signature!");
        }
        this.signature = newSignature;
    }

}
