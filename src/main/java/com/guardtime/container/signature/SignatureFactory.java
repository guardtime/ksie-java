package com.guardtime.container.signature;

import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

public interface SignatureFactory {

    ContainerSignature create(DataHash hash) throws SignatureException;

    ContainerSignature read(InputStream input) throws SignatureException;

    SignatureFactoryType getSignatureFactoryType();

}
