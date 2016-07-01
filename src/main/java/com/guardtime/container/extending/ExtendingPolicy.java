package com.guardtime.container.extending;

import com.guardtime.container.signature.SignatureException;

/**
 * Extending policy to link signature with a trust anchor.
 * @param <O>    Signature class that is extended
 */
public interface ExtendingPolicy<O> {

    /**
     * Returns an extended version of the input signature.
     * @param signature to be extended.
     * @throws SignatureException when extending fails for any reason.
     */
    O getExtendedSignature(O signature) throws SignatureException;
}
