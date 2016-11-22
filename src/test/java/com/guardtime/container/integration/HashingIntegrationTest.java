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
import com.guardtime.util.TestHashAlgorithmProvider;

import org.junit.After;
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
    private final ContainerAnnotation CONTAINER_ANNOTATION = new StringContainerAnnotation(ContainerAnnotationType.FULLY_REMOVABLE, CONTAINER_ANNOTATION_CONTENT, CONTAINER_ANNOTATION_TYPE_DOMAIN);
    private final ContainerDocument CONTAINER_DOCUMENT = new StreamContainerDocument(new ByteArrayInputStream(INPUT_STREAM_STRING.getBytes()), CONTAINER_DOCUMENT_MIME_TYPE, CONTAINER_DOCUMENT_FILE_NAME);
    private Container container;

    @After
    public void cleanUp() throws Exception {
        CONTAINER_ANNOTATION.close();
        CONTAINER_DOCUMENT.close();
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void testCheckNonDefaultHashingAlgorithm() throws Exception {
        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(hashingAlgorithm);
        setUpContainer(provider);

        SignatureContent signatureContent = container.getSignatureContents().get(0);

        Manifest manifest = signatureContent.getManifest().getRight();
        checkDataHashList(hashingAlgorithm, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(hashingAlgorithm, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(hashingAlgorithm, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(hashingAlgorithm, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        Map<String, SingleAnnotationManifest> singleAnnotationManifestMap = signatureContent.getSingleAnnotationManifests();
        for (String key : singleAnnotationManifestMap.keySet()) {
            checkDataHashList(hashingAlgorithm, singleAnnotationManifestMap.get(key).getDocumentsManifestReference().getHashList());
            Assert.assertEquals(hashingAlgorithm, singleAnnotationManifestMap.get(key).getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(hashingAlgorithm, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testUseSeveralHashingAlgorithms() throws Exception {
        List<HashAlgorithm> hashes = Arrays.asList(HashAlgorithm.SHA1,
                HashAlgorithm.RIPEMD_160,
                HashAlgorithm.SHA2_256,
                HashAlgorithm.SHA2_384,
                HashAlgorithm.SHA2_512);
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(
                hashes, hashes, HashAlgorithm.SHA2_256, HashAlgorithm.SHA2_256);

        setUpContainer(provider);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Collection<SingleAnnotationManifest> singleAnnotationManifestValues = signatureContent.getSingleAnnotationManifests().values();

        checkDataHashList(hashes, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(hashes, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(hashes, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(hashes, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (SingleAnnotationManifest value : singleAnnotationManifestValues) {
            checkDataHashList(hashes, value.getDocumentsManifestReference().getHashList());
            Assert.assertEquals(HashAlgorithm.SHA2_256, value.getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(HashAlgorithm.SHA2_256, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testDifferentHashingAlgorithmsForDifferentParts() throws Exception {
        List<HashAlgorithm> fileReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.RIPEMD_160);
        List<HashAlgorithm> documentReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA1);
        HashAlgorithm annotationDataReferenceHashAlgorithm = HashAlgorithm.SHA2_384;
        HashAlgorithm signingHashAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(
                fileReferenceHashAlgorithms,
                documentReferenceHashAlgorithms,
                annotationDataReferenceHashAlgorithm,
                signingHashAlgorithm);

        setUpContainer(provider);
        SignatureContent signatureContent = container.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Collection<SingleAnnotationManifest> singleAnnotationManifestValues = signatureContent.getSingleAnnotationManifests().values();

        checkDataHashList(fileReferenceHashAlgorithms, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(fileReferenceHashAlgorithms, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(documentReferenceHashAlgorithms, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(fileReferenceHashAlgorithms, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (SingleAnnotationManifest value : singleAnnotationManifestValues) {
            checkDataHashList(fileReferenceHashAlgorithms, value.getDocumentsManifestReference().getHashList());
            Assert.assertEquals(annotationDataReferenceHashAlgorithm, value.getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getContainerSignature().getSignature();
        Assert.assertEquals(signingHashAlgorithm, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testUsingNotImplementedHashingAlgorithm() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Hash algorithm SHA3_512 is not implemented");
        HashAlgorithm hashAlgorithm = HashAlgorithm.SHA3_512;
        HashAlgorithmProvider hashAlgorithmProvider = new TestHashAlgorithmProvider(hashAlgorithm);
        createContainer(hashAlgorithmProvider);
    }

    @Test
     public void testUsingNotImplementedHashingAlgorithmInList() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Hash algorithm SM3 is not implemented");
        List<HashAlgorithm> hashAlgorithmList = Arrays.asList(HashAlgorithm.SHA1, HashAlgorithm.SHA2_256, HashAlgorithm.SM3);
        HashAlgorithm hashAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider hashAlgorithmProvider = new TestHashAlgorithmProvider(hashAlgorithmList, hashAlgorithmList, hashAlgorithm, hashAlgorithm);
        createContainer(hashAlgorithmProvider);
    }

    private ZipContainerPackagingFactory getContainerPackagingFactory(HashAlgorithmProvider provider) throws Exception {
        ContainerManifestFactory containerManifestFactory = new TlvContainerManifestFactory(provider);
        return new ZipContainerPackagingFactory(signatureFactory, containerManifestFactory);
    }

    private void setUpContainer(HashAlgorithmProvider provider) throws Exception {
        ContainerBuilder builder = new ContainerBuilder(getContainerPackagingFactory(provider));
        builder.withAnnotation(CONTAINER_ANNOTATION);
        builder.withDocument(CONTAINER_DOCUMENT);
        this.container = builder.build();
    }

    private void checkDataHashList(List<HashAlgorithm> expectedHashAlgorithms, List<DataHash> dataHashes) throws Exception {
        Assert.assertEquals(expectedHashAlgorithms.size(), dataHashes.size());
        List<HashAlgorithm> foundAlgorithms = new LinkedList<>();
        for (DataHash dataHash : dataHashes) {
            foundAlgorithms.add(dataHash.getAlgorithm());
        }
        for (HashAlgorithm expectedAlgorithm : expectedHashAlgorithms) {
            Assert.assertTrue(String.format("Expected hash with algorithm %s was not found.", expectedAlgorithm), foundAlgorithms.contains(expectedAlgorithm));
        }
    }

    private void checkDataHashList(HashAlgorithm expectedHashAlgorithm, List<DataHash> dataHashes) throws Exception {
        checkDataHashList(Arrays.asList(expectedHashAlgorithm), dataHashes);
    }
}
