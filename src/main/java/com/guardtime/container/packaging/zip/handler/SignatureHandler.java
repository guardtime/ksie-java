package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static com.guardtime.container.packaging.EntryNameProvider.SIGNATURE_FORMAT;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;

/**
 * This content holders is used for signatures inside the container.
 */
public class SignatureHandler extends ContentHandler<ContainerSignature> {

    private final SignatureFactory signatureFactory;

    public SignatureHandler(SignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(SIGNATURE_FORMAT, ".+", signatureFactory.getSignatureFactoryType().getSignatureFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected ContainerSignature getEntry(String name) throws ContentParsingException {
        try {
            File file = fetchFileFromEntries(name);
            return signatureFactory.read(Files.newInputStream(file.toPath(), DELETE_ON_CLOSE));
        } catch (SignatureException e) {
            throw new ContentParsingException("Failed to parse content of signature file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
