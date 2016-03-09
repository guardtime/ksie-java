package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ManifestHolder extends IndexedContentHandler<SignatureManifest> {

    private final ContainerManifestFactory manifestFactory;

    public ManifestHolder(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/manifest") || name.startsWith("META-INF/manifest"); //TODO
    }

    @Override
    public SignatureManifest get(String name) {
        File file = entries.get(name);
        try (FileInputStream input = new FileInputStream(file)) {
            return manifestFactory.readSignatureManifest(input);
        } catch (InvalidManifestException | IOException e) {
            throw new RuntimeException(e); //TODO
        }
    }

}
