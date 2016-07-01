package com.guardtime.container.extending;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extending for all signatures in a container.
 * @param <O>   Signature class
 */
public abstract class ContainerSignatureExtender<O> {
    private static final Logger logger = LoggerFactory.getLogger(ContainerSignatureExtender.class);

    /**
     * Extends each signature in input container and returns the container.
     * If a signature extending fails it is logged at INFO level and skipped.
     * @param container    Container to be extended.
     */
    public void extend(Container container) {
        for (SignatureContent content : container.getSignatureContents()) {
            try {
                extendSignatureContent(content);
            } catch (SignatureException e) {
                Manifest manifest = content.getManifest().getRight();
                String signatureUri = manifest.getSignatureReference().getUri();
                logger.info("Failed to extend signature '{}' because: '{}'", signatureUri, e.getMessage());
            }
        }
    }

    private void extendSignatureContent(SignatureContent content) throws SignatureException {
        ContainerSignature containerSignature = content.getContainerSignature();
        if (unsupportedContainerSignature(containerSignature)) {
            throw new SignatureException("Unsupported ContainerSignature provided for extending.");
        }

        O extendedSignature = getExtendedSignature(containerSignature);
        containerSignature.extend(extendedSignature);
    }

    /**
     * Returns true if the provided {@link ContainerSignature} implementation is unsupported for extending.
     * @param containerSignature to be verified
     */
    protected abstract boolean unsupportedContainerSignature(ContainerSignature containerSignature);

    /**
     * Returns the result of signature extending.
     * @param containerSignature to be extended
     * @throws SignatureException when any Exception occurs except RuntimeException.
     */
    protected abstract O getExtendedSignature(ContainerSignature containerSignature) throws SignatureException;
}
