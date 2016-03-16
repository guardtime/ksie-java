package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
    protected AnnotationsManifest getEntry(String name) throws FileParsingException {
        File file = entries.get(name);
        if (file == null) throw new FileParsingException("No file for name '" + name + "'");
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readAnnotationsManifest(input);
        } catch (BlockChainContainerException | IOException e) {
            throw new FileParsingException(e);
        }
    }

    public int getMaxAnnotationManifestIndex() {
        int max = 0;
        for(File file : entries.values()) {
            try {
                AnnotationsManifest manifest = manifestFactory.readAnnotationsManifest(new FileInputStream(file));
                for(FileReference reference : manifest.getAnnotationManifestReferences()) {
                    int index = Util.extractIntegerFrom(reference.getUri());
                    if(index > max) max = index;
                }
            } catch (Exception e) {
                // We don't care about the manifests we can't access, ignore em
            }
        }
        return max;
    }

}
