package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATIONS_MANIFEST_FORMAT;

/**
 * This content holders is used for annotations manifests inside the container.
 */
public class AnnotationsManifestHandler extends ContentHandler<AnnotationsManifest> {

    private final ContainerManifestFactory manifestFactory;

    public AnnotationsManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(ANNOTATIONS_MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected AnnotationsManifest getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (InputStream input = Files.newInputStream(file.toPath())) {
            AnnotationsManifest annotationsManifest = manifestFactory.readAnnotationsManifest(input);
            Files.deleteIfExists(file.toPath());
            return annotationsManifest;
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of annotmanifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
