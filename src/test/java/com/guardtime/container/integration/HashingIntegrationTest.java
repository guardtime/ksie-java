package com.guardtime.container.integration;

import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

public class HashingIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

    private static final String CONTAINER_ANNOTATION_TYPE_DOMAIN = "Some kind of domain";
    private static final String CONTAINER_ANNOTATION_CONTENT = "StringContainerAnnotationTypeIsFullyRemovable";
    private static final String CONTAINER_DOCUMENT_FILE_NAME = "StreamFile.txt";
    private static final String CONTAINER_DOCUMENT_MIME_TYPE = "Stream";
    private static final String INPUT_STREAM_STRING = "Input from stream.";

    @Test
    public void testCheckNonDefaultHashingAlgorithm() throws Exception {
        HashAlgorithmProvider provider = new IntegrationTestHashAlgorithmProvider(HashAlgorithm.SHA2_512);
        Container container = createContainer(provider);

        SignatureContent signatureContent = container.getSignatureContents().get(0);

        Manifest manifest = signatureContent.getManifest().getRight();
        checkDataHashList(HashAlgorithm.SHA2_512, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(HashAlgorithm.SHA2_512, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(HashAlgorithm.SHA2_512, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(HashAlgorithm.SHA2_512, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        Map<String, SingleAnnotationManifest> singleAnnotationManifestMap = signatureContent.getSingleAnnotationManifests();
        for (String key : singleAnnotationManifestMap.keySet()) {
            checkDataHashList(HashAlgorithm.SHA2_512, singleAnnotationManifestMap.get(key).getDocumentsManifestReference().getHashList());
            Assert.assertEquals(HashAlgorithm.SHA2_512, singleAnnotationManifestMap.get(key).getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(HashAlgorithm.SHA2_512, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testUseSeveralHashingAlgorithms() throws Exception {
        List<HashAlgorithm> hashes = Arrays.asList(HashAlgorithm.SHA1,
                HashAlgorithm.RIPEMD_160,
                HashAlgorithm.SHA2_256,
                HashAlgorithm.SHA2_384,
                HashAlgorithm.SHA2_512);
        HashAlgorithmProvider provider = new IntegrationTestHashAlgorithmProvider(
                hashes, hashes, HashAlgorithm.SHA2_256, HashAlgorithm.SHA2_256);

        Container container = createContainer(provider);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Map<String, SingleAnnotationManifest> singleAnnotationManifestMap = signatureContent.getSingleAnnotationManifests();

        checkDataHashList(hashes, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(hashes, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(hashes, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(hashes, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (String key : singleAnnotationManifestMap.keySet()) {
            checkDataHashList(hashes, singleAnnotationManifestMap.get(key).getDocumentsManifestReference().getHashList());
            Assert.assertEquals(HashAlgorithm.SHA2_256, singleAnnotationManifestMap.get(key).getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(HashAlgorithm.SHA2_256, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testDifferentHashingAlgorithmsForDifferentParts() throws Exception {
        HashAlgorithmProvider provider = new IntegrationTestHashAlgorithmProvider(
                Arrays.asList(HashAlgorithm.RIPEMD_160),
                Arrays.asList(HashAlgorithm.SHA1),
                HashAlgorithm.SHA2_384,
                HashAlgorithm.SHA2_512);

        Container container = createContainer(provider);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Map<String, SingleAnnotationManifest> singleAnnotationManifestMap = signatureContent.getSingleAnnotationManifests();

        checkDataHashList(HashAlgorithm.RIPEMD_160, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(HashAlgorithm.RIPEMD_160, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(HashAlgorithm.SHA1, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(HashAlgorithm.RIPEMD_160, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (String key : singleAnnotationManifestMap.keySet()) {
            checkDataHashList(HashAlgorithm.RIPEMD_160, singleAnnotationManifestMap.get(key).getDocumentsManifestReference().getHashList());
            Assert.assertEquals(HashAlgorithm.SHA2_384, singleAnnotationManifestMap.get(key).getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(HashAlgorithm.SHA2_512, signature.getInputHash().getAlgorithm());
    }

    private ZipContainerPackagingFactory getContainerPackagingFactory(HashAlgorithmProvider provider) throws Exception {
        ContainerManifestFactory containerManifestFactory = new TlvContainerManifestFactory(provider);
        return new ZipContainerPackagingFactory(signatureFactory, containerManifestFactory);
    }

    private Container createContainer(HashAlgorithmProvider provider) throws Exception {
        ContainerBuilder builder = new ContainerBuilder(getContainerPackagingFactory(provider));
        ContainerAnnotation containerAnnotation = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, CONTAINER_ANNOTATION_CONTENT, CONTAINER_ANNOTATION_TYPE_DOMAIN);
        ContainerDocument containerDocument = new StreamContainerDocument(new ByteArrayInputStream(INPUT_STREAM_STRING.getBytes()), CONTAINER_DOCUMENT_MIME_TYPE, CONTAINER_DOCUMENT_FILE_NAME);
        builder.withAnnotation(containerAnnotation);
        builder.withDocument(containerDocument);
        return builder.build();
    }

    private void checkDataHashList(List<HashAlgorithm> expectedHashAlgorithms, List<DataHash> dataHashes) throws Exception {
        Assert.assertEquals(expectedHashAlgorithms.size(), dataHashes.size());
        List<HashAlgorithm> foundAlgorithms = new LinkedList<>();
        for (DataHash dataHash : dataHashes){
            foundAlgorithms.add(dataHash.getAlgorithm());
        }
        for (HashAlgorithm expectedAlgorithm : expectedHashAlgorithms) {
            Assert.assertTrue(String.format("Expected hash with algorithm %s was not found.", expectedAlgorithm), foundAlgorithms.contains(expectedAlgorithm));
        }
    }

    private void checkDataHashList(HashAlgorithm expectedHashAlgorithm, List<DataHash> dataHashes) throws Exception {
        checkDataHashList(Arrays.asList(expectedHashAlgorithm), dataHashes);
        Assert.assertEquals(1, dataHashes.size());
    }
}
