package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SignatureHandler extends ContentHandler<ContainerSignature> {

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
    protected ContainerSignature getEntry(String name) throws FileParsingException {
        File file = entries.get(name);
        if (file == null) throw new FileParsingException("No file for name '" + name + "'");
        try (FileInputStream input = new FileInputStream(file)) {
            return signatureFactory.read(input);
        } catch (BlockChainContainerException | IOException e) {
            throw new FileParsingException(e);
        }
    }

}
