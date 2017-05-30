package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.packaging.parsing.store.ParsingStore;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.SIGNATURE_FORMAT;

/**
 * This content holders is used for signatures inside the container.
 */
public class SignatureHandler extends ContentHandler<ContainerSignature> {

    private final SignatureFactory signatureFactory;

    public SignatureHandler(SignatureFactory signatureFactory, ParsingStore store) {
        super(store);
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
        try (InputStream stream = fetchStreamFromEntries(name)) {
            ContainerSignature read = signatureFactory.read(stream);
            parsingStore.remove(name);
            return read;
        } catch (SignatureException e) {
            throw new ContentParsingException("Failed to parse content of '" + name + "'", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }

}
