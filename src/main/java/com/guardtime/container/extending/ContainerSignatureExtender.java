package com.guardtime.container.extending;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Util;

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
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(policy, "Extending policy");
        this.signatureFactory = signatureFactory;
        this.policy = policy;
    }

    /**
     * Extends each signature in input container and returns the container.
     * If a signature extending fails it is logged at INFO level and skipped.
     * @param container    Container to be extended.
     * @return True if all signatures were extended successfully, false otherwise.
     */
    public boolean extend(Container container) {
        boolean status = true;
        for (SignatureContent content : container.getSignatureContents()) {
            Manifest manifest = content.getManifest().getRight();
            String signatureUri = manifest.getSignatureReference().getUri();
            try {
                ContainerSignature containerSignature = content.getContainerSignature();
                signatureFactory.extend(containerSignature, policy);

                if (!containerSignature.isExtended()) {
                    status = false;
                    logger.info("Extending signature '{}' resulted in a non-extended signature without exception!", signatureUri);
                }
            } catch (SignatureException e) {
                status = false;
                logger.info("Failed to extend signature '{}' because: '{}'", signatureUri, e.getMessage());
            }
        }
        return status;
    }

}
