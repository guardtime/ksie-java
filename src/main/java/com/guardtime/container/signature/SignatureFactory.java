package com.guardtime.container.signature;

import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

/**
 * Creates and reads signatures used in container.
 */
public interface SignatureFactory {

    /**
     * Returns signature contained in a {@link ContainerSignature} implementation.
     * @param hash to be signed.
     * @throws SignatureException when creating the signature for the given hash fails.
     */
    ContainerSignature create(DataHash hash) throws SignatureException;

    /**
     * Returns signature contained in a {@link ContainerSignature} implementation.
     * @param input stream from which the signature is to be read.
     * @throws SignatureException reading the stream fails or constructing a signature from the read data fails.
     */
    ContainerSignature read(InputStream input) throws SignatureException;

    /**
     * Updates the {@link ContainerSignature} to extend its underlying signature to a trust anchor
     * @param containerSignature The signature to be extended.
     * @param extender The extending logic for the underlying signature inside containerSignature.
     * @throws SignatureException when the extending fails for any reason.
     */
    void extend(ContainerSignature containerSignature, ExtendingPolicy extender) throws SignatureException;

    SignatureFactoryType getSignatureFactoryType();

}
