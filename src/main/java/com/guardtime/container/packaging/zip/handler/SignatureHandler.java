package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SignatureHandler extends IndexedContentHandler<ContainerSignature> {

    private final SignatureFactory signatureFactory;

    public SignatureHandler(SignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "signature[0-9]+." + signatureFactory.getSignatureFactoryType().getSignatureFileExtension());
    }

    @Override
    public ContainerSignature get(String name) {
        try {
            File file = entries.get(name);
            return signatureFactory.read(new FileInputStream(file));
        } catch (BlockChainContainerException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
