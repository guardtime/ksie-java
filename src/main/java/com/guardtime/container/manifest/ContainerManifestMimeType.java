package com.guardtime.container.manifest;

public enum ContainerManifestMimeType {
    SIGNATURE_MANIFEST("application/ksi-signature"),
    ANNOTATIONS_MANIFEST("ksie10/annotmanifest"),
    DATA_MANIFEST("ksie10/datamanifest");

    private String type;

    ContainerManifestMimeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
