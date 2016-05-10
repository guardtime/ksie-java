package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.Manifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This content holders is used for manifests inside the container.
 */
public class ManifestHandler extends IndexedContentHandler<Manifest> {

    private final ContainerManifestFactory manifestFactory;

    public ManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "manifest[0-9]+." + manifestFactory.getManifestFactoryType().getManifestFileExtension());
    }

    @Override
    protected Manifest getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of manifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
