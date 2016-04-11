package com.guardtime.container.extending;

import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that deals with extending all signatures in a container based on the signature extending logic provided
 * by passed in {@link SignatureExtender} implementation.
 */
public class BlockChainContainerExtender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainContainerExtender.class);

    private final SignatureExtender extender;

    public BlockChainContainerExtender(SignatureExtender extender) {
        Util.notNull(extender, "Signature extender");
        this.extender = extender;
    }

    /**
     * Extends each signature in input container and returns the container.
     * @param container    Container to be extended.
     * @return Input container with extended signatures.
     */
    public BlockChainContainer extend(BlockChainContainer container) {
        for (SignatureContent content : container.getSignatureContents()) {
            if (!content.extendSignature(extender)) {
                SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
                String signatureUri = signatureManifest.getSignatureReference().getUri();
                LOGGER.info("Failed to extend signature '{}'", signatureUri);
            }
        }
        return container;
    }
}
