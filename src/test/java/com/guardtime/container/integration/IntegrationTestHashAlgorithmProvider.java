package com.guardtime.container.integration;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Arrays;
import java.util.List;

public class IntegrationTestHashAlgorithmProvider implements HashAlgorithmProvider{
    private List<HashAlgorithm> fileReferenceHashAlgorithms;
    private List<HashAlgorithm> documentReferenceHashAlgorithms;
    private HashAlgorithm annotationDataReferenceHashAlgorithm;
    private HashAlgorithm signingHashAlgorithm;

    public IntegrationTestHashAlgorithmProvider() throws Exception {
        this.fileReferenceHashAlgorithms = Arrays.asList(HashAlgorithm.SHA2_256);
        this.documentReferenceHashAlgorithms = Arrays.asList(HashAlgorithm.SHA2_256);
        this.annotationDataReferenceHashAlgorithm = HashAlgorithm.SHA2_256;
        this.signingHashAlgorithm = HashAlgorithm.SHA2_256;
    }

    public IntegrationTestHashAlgorithmProvider(HashAlgorithm algorithm) throws Exception {
        Util.notNull(algorithm, " Hashing algorithm");
        this.fileReferenceHashAlgorithms = Arrays.asList(algorithm);
        this.documentReferenceHashAlgorithms = Arrays.asList(algorithm);
        this.annotationDataReferenceHashAlgorithm = algorithm;
        this.signingHashAlgorithm = algorithm;
    }

    public IntegrationTestHashAlgorithmProvider(List<HashAlgorithm> fileReferenceHashAlgorithms,
                                                List<HashAlgorithm> documentReferenceHashAlgorithms,
                                                HashAlgorithm annotationDataReferenceHashAlgorithm,
                                                HashAlgorithm signingHashAlgorithm) throws Exception {
        Util.notNull(fileReferenceHashAlgorithms, "fileReferenceHashAlgorithms");
        Util.notNull(documentReferenceHashAlgorithms, "documentReferenceHashAlgorithms");
        Util.notNull(annotationDataReferenceHashAlgorithm, "annotationDataReferenceHashAlgorithm");
        Util.notNull(signingHashAlgorithm, "signingHashAlgorithm");
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
