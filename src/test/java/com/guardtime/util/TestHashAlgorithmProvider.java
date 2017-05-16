package com.guardtime.util;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Collections;
import java.util.List;

public class TestHashAlgorithmProvider implements HashAlgorithmProvider {
    private List<HashAlgorithm> fileReferenceHashAlgorithms;
    private List<HashAlgorithm> documentReferenceHashAlgorithms;
    private HashAlgorithm annotationDataReferenceHashAlgorithm;
    private HashAlgorithm signingHashAlgorithm;

    public TestHashAlgorithmProvider() throws Exception {
        this.fileReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA2_256);
        this.documentReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA2_256);
        this.annotationDataReferenceHashAlgorithm = HashAlgorithm.SHA2_256;
        this.signingHashAlgorithm = HashAlgorithm.SHA2_256;
    }

    public TestHashAlgorithmProvider(HashAlgorithm algorithm) throws Exception {
        this.fileReferenceHashAlgorithms = Collections.singletonList(algorithm);
        this.documentReferenceHashAlgorithms = Collections.singletonList(algorithm);
        this.annotationDataReferenceHashAlgorithm = algorithm;
        this.signingHashAlgorithm = algorithm;
    }

    public TestHashAlgorithmProvider(List<HashAlgorithm> fileReferenceHashAlgorithms,
                                     List<HashAlgorithm> documentReferenceHashAlgorithms,
                                     HashAlgorithm annotationDataReferenceHashAlgorithm,
                                     HashAlgorithm signingHashAlgorithm) throws Exception {
        this.fileReferenceHashAlgorithms = fileReferenceHashAlgorithms;
        this.documentReferenceHashAlgorithms = documentReferenceHashAlgorithms;
        this.annotationDataReferenceHashAlgorithm = annotationDataReferenceHashAlgorithm;
        this.signingHashAlgorithm = signingHashAlgorithm;

    }

    @Override
    public List<HashAlgorithm> getFileReferenceHashAlgorithms() {
        return fileReferenceHashAlgorithms;
    }

    @Override
    public List<HashAlgorithm> getDocumentReferenceHashAlgorithms() {
        return documentReferenceHashAlgorithms;
    }

    @Override
    public HashAlgorithm getAnnotationDataReferenceHashAlgorithm() {
        return annotationDataReferenceHashAlgorithm;
    }

    @Override
    public HashAlgorithm getSigningHashAlgorithm() {
        return signingHashAlgorithm;
    }
}
