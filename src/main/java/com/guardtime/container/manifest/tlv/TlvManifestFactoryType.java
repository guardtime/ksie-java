package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.ManifestFactoryType;

class TlvManifestFactoryType implements ManifestFactoryType {

    private final String name;
    private final String manifestFileExtension;

    public TlvManifestFactoryType(String name, String manifestFileExtension) {
        this.name = name;
        this.manifestFileExtension = manifestFileExtension;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getManifestFileExtension() {
        return manifestFileExtension;
    }

    @Override
    public String toString() {
        return "ManifestFactory{" +
                "name='" + name + '\'' +
                ", fileExtension='" + manifestFileExtension + '\'' +
                '}';
    }

}
