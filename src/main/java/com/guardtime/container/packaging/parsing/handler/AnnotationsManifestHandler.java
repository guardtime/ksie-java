package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.packaging.parsing.store.ParsingStore;

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
            AnnotationsManifest annotationsManifest = manifestFactory.readAnnotationsManifest(input);
            parsingStore.remove(name);
            return annotationsManifest;
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of '" + name + "'", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read content of '" + name + "'", e);
        }
    }

}
