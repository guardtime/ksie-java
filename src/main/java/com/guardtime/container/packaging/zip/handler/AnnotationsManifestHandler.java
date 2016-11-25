package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.packaging.zip.parsing.ParsingStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.packaging.EntryNameProvider.ANNOTATIONS_MANIFEST_FORMAT;

/**
 * This content holders is used for annotations manifests inside the container.
 */
public class AnnotationsManifestHandler extends ContentHandler<AnnotationsManifest> {

    private final ContainerManifestFactory manifestFactory;

    public AnnotationsManifestHandler(ContainerManifestFactory manifestFactory, ParsingStore store) {
        super(store);
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
        try (InputStream input = fetchStreamFromEntries(name)) {
            return manifestFactory.readAnnotationsManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of annotmanifest file", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
