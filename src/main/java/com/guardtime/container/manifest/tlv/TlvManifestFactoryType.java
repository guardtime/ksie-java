package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.ManifestFactoryType;

import java.util.Objects;

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
        return this.getClass().getSimpleName() + " {" +
                "name= \'" + name + '\'' +
                ", fileExtension= \'" + manifestFileExtension + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlvManifestFactoryType that = (TlvManifestFactoryType) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(manifestFileExtension, that.manifestFileExtension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, manifestFileExtension);
    }
}
