package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AnnotationManifestHandler extends ContentHandler<AnnotationInfoManifest> {

    private final ContainerManifestFactory manifestFactory;

    public AnnotationManifestHandler(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/annotation"); //TODO
    }

    @Override
    public AnnotationInfoManifest get(String name) {
        File file = entries.get(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readAnnotationManifest(input);
        } catch (InvalidManifestException | IOException e) {
            throw new RuntimeException(e); //TODO
        }
    }

}
