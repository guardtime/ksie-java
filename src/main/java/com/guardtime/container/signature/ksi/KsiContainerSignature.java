package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.ksi.unisignature.KSISignature;

public class KsiContainerSignature implements ContainerSignature {

    private final KSISignature signature;

    public KsiContainerSignature(KSISignature signature) {
        this.signature = signature;
    }

    public KSISignature getSignature() {
        return signature;
    }
}
