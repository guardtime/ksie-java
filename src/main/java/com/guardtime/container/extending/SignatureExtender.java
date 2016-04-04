package com.guardtime.container.extending;

import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;

public interface SignatureExtender {
    ContainerSignature extend(ContainerSignature signature) throws SignatureException;
}
