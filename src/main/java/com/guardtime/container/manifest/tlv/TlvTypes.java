package com.guardtime.container.manifest.tlv;

public enum TlvTypes {
    DATA_FILES_MANIFEST_REFERENCE(0xb01),
    ANNOTATIONS_MANIFEST_REFERENCE(0xb02),
    DATA_FILE_REFERENCE(0xb03),
    ANNOTATION_INFO_REFERENCE(0xb04),
    ANNOTATION_REFERENCE(0xb05),
    SIGNATURE_REFERENCE(0xb06);

    private int type;

    TlvTypes(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
