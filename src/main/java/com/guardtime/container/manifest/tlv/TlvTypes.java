package com.guardtime.container.manifest.tlv;

import java.util.HashMap;
import java.util.Map;

enum TlvTypes {
    DATA_FILES_MANIFEST_REFERENCE(0xb01),
    ANNOTATIONS_MANIFEST_REFERENCE(0xb02),
    DATA_FILE_REFERENCE(0xb03),
    ANNOTATION_INFO_REFERENCE(0xb04),
    ANNOTATION_REFERENCE(0xb05),
    SIGNATURE_REFERENCE(0xb06);

    private static Map<Integer, TlvTypes> map = new HashMap<>();
    private int type;

    static {
        for (TlvTypes type : TlvTypes.values()) {
            map.put(type.getType(), type);
        }
    }

    public static TlvTypes fromValue(int value) {
        return map.get(value);
    }

    TlvTypes(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
