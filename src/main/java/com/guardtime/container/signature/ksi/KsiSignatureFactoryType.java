package com.guardtime.container.signature.ksi;

import com.guardtime.container.signature.SignatureFactoryType;

class KsiSignatureFactoryType implements SignatureFactoryType {

    private static final String KSI_SIGNATURE_MIME_TYPE = "application/ksi-signature";
    private static final String KSI_SIGNATURE_FILE_EXTENSION = "ksi";
    private static final String KSI_SIGNATURE_FACTORY_NAME = "KSI signature factory";

    @Override
    public String getName() {
        return KSI_SIGNATURE_FACTORY_NAME;
    }

    @Override
    public String getSignatureFileExtension() {
        return KSI_SIGNATURE_FILE_EXTENSION;
    }

    @Override
    public String getSignatureMimeType() {
        return KSI_SIGNATURE_MIME_TYPE;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + " {" +
                "name='" + KSI_SIGNATURE_FACTORY_NAME + '\'' +
                ", fileExtension='" + KSI_SIGNATURE_FILE_EXTENSION + "\'}";
    }
}
