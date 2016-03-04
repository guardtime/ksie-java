package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataManifestHandler extends ContentHandler<DataFilesManifest> {

    private final ContainerManifestFactory manifestFactory;

    public DataManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesDirectory(name, "META-INF") &&
                fileNameStartsWith(name, "datamanifest");
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

}
