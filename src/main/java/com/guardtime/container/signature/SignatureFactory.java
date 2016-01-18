package com.guardtime.container.signature;

import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

public interface SignatureFactory<T extends ContainerSignature> {

    T create(DataHash hash) throws SignatureException;

    T read(InputStream input) throws SignatureException;

}
