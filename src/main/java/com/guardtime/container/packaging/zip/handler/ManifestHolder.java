package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SignatureManifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ManifestHolder extends ContentHandler<SignatureManifest> {
    private int maxIndex = 0;

    private final ContainerManifestFactory manifestFactory;

    public ManifestHolder(ContainerManifestFactory manifestFactory) {
        this.manifestFactory = manifestFactory;
    }

    @Override
    public boolean isSupported(String name) {
        return name.startsWith("/META-INF/manifest") || name.startsWith("META-INF/manifest"); //TODO
    }

    @Override
    public void add(String name, File file) {
        super.add(name, file);
        int index = Integer.parseInt(name.replaceAll("[^0-9]", ""));
        if(index > maxIndex) maxIndex = index;
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

    public int getMaxIndex() {
        return maxIndex;
    }

}
