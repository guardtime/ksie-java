package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
    public AnnotationsManifest get(String name) {
        try {
            File file = entries.get(name);
            return manifestFactory.readAnnotationsManifest(new FileInputStream(file));
        } catch (BlockChainContainerException | FileNotFoundException e) {
            throw new RuntimeException(e);
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
