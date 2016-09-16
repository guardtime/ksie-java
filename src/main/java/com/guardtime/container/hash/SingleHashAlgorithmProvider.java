package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Arrays;
import java.util.List;

public class SingleHashAlgorithmProvider implements HashAlgorithmProvider {
    private final HashAlgorithm algorithm;

    public SingleHashAlgorithmProvider(HashAlgorithm hashAlgorithm) {
        if (!hashAlgorithm.getStatus().equals(HashAlgorithm.Status.NORMAL)) {
            throw new IllegalArgumentException("Invalid HashAlgorithm provided! Only accept with status 'NORMAL', not '" + hashAlgorithm.getStatus() + "'");
        }
        this.algorithm = hashAlgorithm;
    }

    @Override
    public List<HashAlgorithm> getFileReferenceHashAlgorithms() {
        return Arrays.asList(algorithm);
    }

    @Override
    public List<HashAlgorithm> getDocumentReferenceHashAlgorithms() {
        return Arrays.asList(algorithm);
    }

    @Override
    public HashAlgorithm getAnnotationDataReferenceHashAlgorithm() {
        return algorithm;
    }

    @Override
    public HashAlgorithm getSignatureHashAlgorithm() {
        return algorithm;
    }
}
