package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.IOException;
import java.io.OutputStream;

public class KsiContainerSignature implements ContainerSignature {

    private final KSISignature signature;

    public KsiContainerSignature(KSISignature signature) {
        this.signature = signature;
    }

    public KSISignature getSignature() {
        return signature;
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
