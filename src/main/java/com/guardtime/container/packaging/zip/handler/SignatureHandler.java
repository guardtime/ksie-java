package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
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
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "signature[0-9]+." + signatureFactory.getSignatureFactoryType().getSignatureFileExtension());
    }

    @Override
    public ContainerSignature get(String name) throws ContentParsingException {
        try {
            File file = entries.get(name);
            return signatureFactory.read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (SignatureException e) {
            throw new ContentParsingException("Failed to parse content of signature file", e);
        }
    }

}
