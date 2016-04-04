package com.guardtime.container.extending;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainContainerExtender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainContainerExtender.class);

    private final SignatureExtender extender;

    public BlockChainContainerExtender(SignatureExtender extender) {
        this.extender = extender;
    }

    public BlockChainContainer extend(BlockChainContainer container) {
        for (SignatureContent content : container.getSignatureContents()) {
            if (!content.extendSignature(extender)) {
                String signatureUri = content.getSignatureManifest().getRight().getSignatureReference().getUri();
                LOGGER.info("Failed to extend signature '{}'", signatureUri);
            }
        }
        return container;
    }
}
