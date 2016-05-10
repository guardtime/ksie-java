package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This content holders is used for annotations manifests inside the container.
 */
public class AnnotationsManifestHandler extends IndexedContentHandler<AnnotationsManifest> {

    private final ContainerManifestFactory manifestFactory;

    public AnnotationsManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "annotmanifest[0-9]+." + manifestFactory.getManifestFactoryType().getManifestFileExtension());
    }

    @Override
    protected AnnotationsManifest getEntry(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readAnnotationsManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of annotmanifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

    public int getMaxSingleAnnotationManifestIndex() {
        int max = 0;
        for (File file : entries.values()) {
            try {
                AnnotationsManifest manifest = manifestFactory.readAnnotationsManifest(new FileInputStream(file));
                for (FileReference reference : manifest.getSingleAnnotationManifestReferences()) {
                    int index = Util.extractIntegerFrom(reference.getUri());
                    if (index > max) max = index;
                }
            } catch (Exception e) {
                // We don't care about the manifests we can't access, ignore them
            }
        }
        return max;
    }

}
