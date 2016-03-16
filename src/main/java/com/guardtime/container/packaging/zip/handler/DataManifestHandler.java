package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DataManifestHandler extends ContentHandler<DataFilesManifest> {

    private final ContainerManifestFactory manifestFactory;

    public DataManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "datamanifest[0-9]+." + manifestFactory.getManifestFactoryType().getManifestFileExtension());
    }

    @Override
    public DataFilesManifest get(String name) throws ContentParsingException {
        File file = fetchFileFromEntries(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readDataFilesManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of datamanifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
