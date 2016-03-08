package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataManifestHandler extends ContentHandler<DataFilesManifest> {
    private int maxIndex = 0;

    private final ContainerManifestFactory manifestFactory;

    public DataManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/datamanifest"); //TODO
    }

    @Override
    public void add(String name, File file) {
        super.add(name, file);
        int index = Util.extractIntegerFrom(name);
        if (index > maxIndex) maxIndex = index;
    }

    @Override
    public DataFilesManifest get(String name) {
        File file = entries.get(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readDataFilesManifest(input);
        } catch (InvalidManifestException | IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public int getMaxIndex() {
        return maxIndex;
    }

}
