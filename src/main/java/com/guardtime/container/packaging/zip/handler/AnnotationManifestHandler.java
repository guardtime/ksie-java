package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AnnotationManifestHandler extends ContentHandler<AnnotationInfoManifest> {

    private final ContainerManifestFactory manifestFactory;

    public AnnotationManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return matchesSingleDirectory(name, "META-INF") &&
                fileNameMatches(name, "annotation[0-9]+." + manifestFactory.getManifestFactoryType().getManifestFileExtension());
    }

    @Override
    public AnnotationInfoManifest get(String name) throws ContentParsingException {
        File file = entries.get(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readAnnotationManifest(input);
        } catch (InvalidManifestException e) {
            throw new ContentParsingException("Failed to parse content of annotation manifest file", e);
        } catch (FileNotFoundException e) {
            throw new ContentParsingException("Failed to locate requested file in filesystem", e);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to read file", e);
        }
    }

}
