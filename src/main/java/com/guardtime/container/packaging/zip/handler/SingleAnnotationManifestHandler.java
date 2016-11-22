package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SingleAnnotationManifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.guardtime.container.packaging.EntryNameProvider.SINGLE_ANNOTATION_MANIFEST_FORMAT;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;

/**
 * This content holders is used for annotation manifests inside the container.
 */
public class SingleAnnotationManifestHandler extends ContentHandler<SingleAnnotationManifest> {

    private final ContainerManifestFactory manifestFactory;

    public SingleAnnotationManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        String regex = String.format(SINGLE_ANNOTATION_MANIFEST_FORMAT, ".+", manifestFactory.getManifestFactoryType().getManifestFileExtension());
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, regex);
    }

    @Override
    protected SingleAnnotationManifest getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (InputStream input = Files.newInputStream(file.toPath(), DELETE_ON_CLOSE)) {
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
