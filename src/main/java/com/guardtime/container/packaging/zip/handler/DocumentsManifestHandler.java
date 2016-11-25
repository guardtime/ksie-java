package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.packaging.zip.parsing.ParsingStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.DOCUMENTS_MANIFEST_FORMAT;

/**
 * This content holders is used for data files manifests inside the container.
 */
public class DocumentsManifestHandler extends ContentHandler<DocumentsManifest> {

    private final ContainerManifestFactory manifestFactory;

    public DocumentsManifestHandler(ContainerManifestFactory manifestFactory, ParsingStore store) {
        super(store);
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(DOCUMENTS_MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected DocumentsManifest getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            return manifestFactory.readDocumentsManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of datamanifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
