package com.guardtime.container.manifest;

//TODO remove?
public enum ContainerManifestMimeType {

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
