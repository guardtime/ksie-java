package com.guardtime.container.extending;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;

public interface SignatureExtender {
    /**
     * Used for extending a single signature.
     * @param signature    Input signature to be extended
     * @return New {@link ContainerSignature} that represents the extended input signature.
     * @throws SignatureException when extending fails for any reason.
     */
    ContainerSignature extend(ContainerSignature signature) throws SignatureException;
}
