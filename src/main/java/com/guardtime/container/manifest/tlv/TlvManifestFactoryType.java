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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TlvManifestFactoryType that = (TlvManifestFactoryType) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return manifestFileExtension != null ? manifestFileExtension.equals(that.manifestFileExtension) : that.manifestFileExtension == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (manifestFileExtension != null ? manifestFileExtension.hashCode() : 0);
        return result;
    }
}
