package com.guardtime.container.extending;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;

/**
 * Common interface for all signature extending logic.
 */
public interface SignatureExtender {
    /**
     * Returns New {@link ContainerSignature} that represents the extended input signature.
     * @param signature Input signature to be extended
     * @throws SignatureException when extending fails for any reason.
     */
    ContainerSignature extend(ContainerSignature signature) throws SignatureException;

    /**
     * Returns true if passed in {@link ContainerSignature} is supported for extending by the implementation.
     */
    boolean isSupported(ContainerSignature signature);
}
