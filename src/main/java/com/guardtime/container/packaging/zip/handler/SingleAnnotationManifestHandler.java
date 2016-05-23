package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This content holders is used for annotation manifests inside the container.
 */
public class SingleAnnotationManifestHandler extends IndexedContentHandler<SingleAnnotationManifest> {

    private final ContainerManifestFactory manifestFactory;

    public SingleAnnotationManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "annotation[0-9]+." + manifestFactory.getManifestFactoryType().getManifestFileExtension());
    }

    @Override
    protected SingleAnnotationManifest getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readSingleAnnotationManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of annotation manifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}