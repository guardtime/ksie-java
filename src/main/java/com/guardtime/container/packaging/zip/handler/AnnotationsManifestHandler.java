package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AnnotationsManifestHandler extends ContentHandler<AnnotationsManifest> {

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

}
