package com.guardtime.container.extending;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.SignatureFactory;

public class BlockChainContainerExtender {
    private final SignatureFactory signatureFactory;

    public BlockChainContainerExtender(SignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    public BlockChainContainer extend(BlockChainContainer container){
        for(SignatureContent content : container.getSignatureContents()){
            content.extendSignature(signatureFactory);
        }
        return container;
    }
}
