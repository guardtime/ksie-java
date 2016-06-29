package com.guardtime.container.signature;

/**
 * Helper class used to convey signature file extensions and MIME type.
 */
public interface SignatureFactoryType {

    String getName();

    String getSignatureFileExtension();

    String getSignatureMimeType();

}
