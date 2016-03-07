package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AnnotationsManifestHandler extends ContentHandler<AnnotationsManifest> {
    private int maxIndex = 0;

    private final ContainerManifestFactory manifestFactory;

    public AnnotationsManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/annotmanifest"); //TODO
    }

    @Override
    public void add(String name, File file) {
        super.add(name, file);
        int index = Integer.parseInt(name.replaceAll("[^0-9]", ""));
        if(index > maxIndex) maxIndex = index;
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

    public int getMaxIndex() {
        return maxIndex;
    }

}
