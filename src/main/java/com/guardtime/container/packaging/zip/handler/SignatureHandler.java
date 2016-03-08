package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SignatureHandler extends ContentHandler<ContainerSignature> {

    private final SignatureFactory signatureFactory;

    public SignatureHandler(SignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/signature");
    }

    @Override
    public ContainerSignature get(String name) throws FileParsingException {
        try {
            File file = entries.get(name);
            return signatureFactory.read(new FileInputStream(file));
        } catch (BlockChainContainerException | FileNotFoundException e) {
            throw new FileParsingException(e);
        }
    }

}
