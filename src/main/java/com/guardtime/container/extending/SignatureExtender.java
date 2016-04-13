package com.guardtime.container.extending;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;

/**
 * Common interface for all signature extending logic.
 */
public interface SignatureExtender {
    /**
     * @param signature
     *         Input signature to be extended
     * @return New {@link ContainerSignature} that represents the extended input signature.
     * @throws SignatureException
     *         when extending fails for any reason.
     */
    ContainerSignature extend(ContainerSignature signature) throws SignatureException;
}
