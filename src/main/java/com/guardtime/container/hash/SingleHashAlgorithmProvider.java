package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple {@link HashAlgorithmProvider} that provides the same {@link HashAlgorithm} for every output.
 */
public class SingleHashAlgorithmProvider implements HashAlgorithmProvider {
    private final HashAlgorithm algorithm;

    /**
     * @param hashAlgorithm    The {@link HashAlgorithm} to be used as output by the created instance.
     */
    public SingleHashAlgorithmProvider(HashAlgorithm hashAlgorithm) {
        if (!hashAlgorithm.getStatus().equals(HashAlgorithm.Status.NORMAL)) {
            throw new IllegalArgumentException("Invalid HashAlgorithm provided! Only accept with status 'NORMAL', not '" + hashAlgorithm.getStatus() + "'");
        }
        this.algorithm = hashAlgorithm;
    }

    @Override
    public List<HashAlgorithm> getFileReferenceHashAlgorithms() {
        return Collections.singletonList(algorithm);
    }

    @Override
    public List<HashAlgorithm> getDocumentReferenceHashAlgorithms() {
        return Collections.singletonList(algorithm);
    }

    @Override
    public HashAlgorithm getAnnotationDataReferenceHashAlgorithm() {
        return algorithm;
    }

    @Override
    public HashAlgorithm getSigningHashAlgorithm() {
        return algorithm;
    }
}
