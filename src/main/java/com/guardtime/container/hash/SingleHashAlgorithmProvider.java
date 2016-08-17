package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.Arrays;
import java.util.List;

public class SingleHashAlgorithmProvider implements HashAlgorithmProvider {
    private final HashAlgorithm algorithm;

    public SingleHashAlgorithmProvider(HashAlgorithm hashAlgorithm) {
        if(!hashAlgorithm.getStatus().equals(HashAlgorithm.Status.NORMAL)) {
            throw new IllegalArgumentException("Invalid HashAlgorithm provided! Only accept with status 'NORMAL', not '" + hashAlgorithm.getStatus() + "'");
        }
        this.algorithm = hashAlgorithm;
    }

    @Override
    public HashAlgorithm getMainHashAlgorithm() {
        return algorithm;
    }

    @Override
    public List<HashAlgorithm> getHashAlgorithms() {
        return Arrays.asList(getMainHashAlgorithm());
    }
}
