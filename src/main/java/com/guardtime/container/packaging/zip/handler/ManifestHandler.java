package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.Manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.guardtime.container.packaging.EntryNameProvider.MANIFEST_FORMAT;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;

/**
 * This content holders is used for manifests inside the container.
 */
public class ManifestHandler extends ContentHandler<Manifest> {

    private final ContainerManifestFactory manifestFactory;

    public ManifestHandler(ContainerManifestFactory manifestFactory) {
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
        File file = fetchFileFromEntries(name);
        try (InputStream input = Files.newInputStream(file.toPath(), DELETE_ON_CLOSE)) {
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
