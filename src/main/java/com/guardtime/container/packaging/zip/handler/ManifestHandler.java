package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.MANIFEST_FORMAT;

/**
 * This content holders is used for manifests inside the container.
 */
public class ManifestHandler extends ContentHandler<Manifest> {

    private final ContainerManifestFactory manifestFactory;

    public ManifestHandler(ContainerManifestFactory manifestFactory, ParsingStore store) {
        super(store);
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected Manifest getEntry(String name) throws ContentParsingException {
        try (InputStream input = fetchStreamFromEntries(name)) {
            Manifest manifest = manifestFactory.readManifest(input);
            parsingStore.remove(name);
            return manifest;
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of '" + name + "'", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }

}
