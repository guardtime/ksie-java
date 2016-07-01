package com.guardtime.container.extending;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extending for all signatures in a container.
 */
public class ContainerSignatureExtender {
    private static final Logger logger = LoggerFactory.getLogger(ContainerSignatureExtender.class);
    private final SignatureFactory signatureFactory;
    private final ExtendingPolicy policy;

    public ContainerSignatureExtender(SignatureFactory signatureFactory, ExtendingPolicy policy) {
        this.signatureFactory = signatureFactory;
        this.policy = policy;
    }

    /**
     * Extends each signature in input container and returns the container.
     * If a signature extending fails it is logged at INFO level and skipped.
     * @param container    Container to be extended.
     */
    public void extend(Container container) {
        for (SignatureContent content : container.getSignatureContents()) {
            try {
                signatureFactory.extend(content.getContainerSignature(), policy);
            } catch (SignatureException e) {
                Manifest manifest = content.getManifest().getRight();
                String signatureUri = manifest.getSignatureReference().getUri();
                logger.info("Failed to extend signature '{}' because: '{}'", signatureUri, e.getMessage());
            }
        }
    }

}
