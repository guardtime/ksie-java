package com.guardtime.envelope.integration;

import com.guardtime.envelope.EnvelopeBuilder;
import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStore;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.packaging.zip.ZipEnvelopeWriter;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.envelope.verification.policy.LimitedInternalVerificationPolicy;
import com.guardtime.ksi.SignatureReader;
import com.guardtime.ksi.Signer;
import com.guardtime.ksi.SignerBuilder;
import com.guardtime.ksi.service.KSISigningClientServiceAdapter;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.CredentialsAwareHttpSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpSigningClient;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.ContextAwarePolicyAdapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;

public class EnvelopeIntegrationTest extends AbstractCommonIntegrationTest {

    private EnvelopePackagingFactory limitedPackagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        limitedPackagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withParsingStore(parsingStore)
                .withIndexProviderFactory(new IncrementingIndexProviderFactory())
                .withVerificationPolicy(new LimitedInternalVerificationPolicy())
                .build();
    }

    @Test
    public void testCreateWithInvalidEndpoint_ThrowsSignatureException() throws Exception {
        CredentialsAwareHttpSettings settings = new CredentialsAwareHttpSettings(
                "http://127.0.0.1:1234",
                new KSIServiceCredentials("LoginId", "LoginKey")
        );
        SimpleHttpSigningClient signingClient = new SimpleHttpSigningClient(settings);

        Signer signer = new SignerBuilder()
                .setDefaultVerificationPolicy(ContextAwarePolicyAdapter.createInternalPolicy())
                .setSigningService(new KSISigningClientServiceAdapter(signingClient))
                .build();

        EnvelopePackagingFactory factory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(new KsiSignatureFactory(signer, new SignatureReader()))
                .withParsingStore(parsingStore)
                .build();

        expectedException.expect(SignatureException.class);
        expectedException.expectMessage("HTTP request failed");
        try (
                Envelope ignored = new EnvelopeBuilder(factory)
                        .withDocument(
                                documentFactory.create(
                                        new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)),
                                        "application/txt",
                                        TEST_FILE_NAME_TEST_TXT
                                )
                        )
                        .build()
        ) {
            //empty
        }
    }

    @Test
    public void testContentOrderWithTwoContents() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_RANDOM_UUID_INDEXES));
                Envelope existingEnvelope = packagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = constructDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));

            try (Envelope second = packagingFactory.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            )) {
                Assert.assertTrue(compareSignatureContentListOrder(second));
            }
        }
    }

    @Test
    public void testContentOrderWithThreeContents() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS));
                Envelope existingEnvelope = packagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = constructDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));

            try (Envelope second = packagingFactory.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            )) {
                Assert.assertTrue(compareSignatureContentListOrder(second));
            }
        }
    }

    @Test
    public void testDocumentOrderWithSameSigningTimes() throws Exception {
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MULTIPLE_SIGNATURES_WITH_SAME_SIGNING_TIME));
             Envelope envelope = packagingFactory.read(stream);
             ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
             Document document = constructDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc");
             Envelope second = packagingFactory.addSignature(
                     envelope,
                     singletonList(document),
                     singletonList(stringEnvelopeAnnotation)
             )
        ) {
            KSISignature signature0 = (KSISignature) second.getSignatureContents().get(0).getEnvelopeSignature().getSignature();
            KSISignature signature1 = (KSISignature) second.getSignatureContents().get(1).getEnvelopeSignature().getSignature();
            KSISignature signature2 = (KSISignature) second.getSignatureContents().get(2).getEnvelopeSignature().getSignature();

            Assert.assertFalse(signature0.getAggregationTime().after(signature1.getAggregationTime()));
            Assert.assertFalse(signature0.getAggregationTime().before(signature1.getAggregationTime()));

            Assert.assertTrue(signature2.getAggregationTime().after(signature0.getAggregationTime()));
            Assert.assertTrue(signature2.getAggregationTime().after(signature1.getAggregationTime()));

            Assert.assertTrue(second.getSignatureContents().get(0).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-1.ksi"));
            Assert.assertTrue(second.getSignatureContents().get(1).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-2.ksi"));
            Assert.assertTrue(second.getSignatureContents().get(2).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-3.ksi"));
        }
    }

    @Test
    public void testInternalFileReferencedAsDocument() throws  Exception {
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_INTERNAL_FILE_AS_DOC_REFERENCE));
             Envelope envelope = packagingFactory.read(stream)
        ) {
            SignatureContent content = envelope.getSignatureContents().get(1);
            content.detachDocument(content.getDocuments().values().iterator().next().getFileName(), documentFactory);

            writeEnvelopeToAndReadFromStream(envelope, packagingFactory);
        }
    }

    @Test
    public void testNotUsedInternalFileReferencedAsDocument() throws  Exception {
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_UNUSED_INTERNAL_FILE_AS_DOC_REFERENCE));
             Envelope envelope = packagingFactory.read(stream)
        ) {
            writeEnvelopeToAndReadFromStream(envelope, packagingFactory);
        }
    }

    @Test
    public void testNotUsedInternalFileReferencedAsDocumentAndAdd_ThrowsIllegalArgumentException() throws  Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File name is not valid! File name: META-INF/manifest-3.tlv");
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_UNUSED_INTERNAL_FILE_AS_DOC_REFERENCE));
             Envelope envelope = limitedPackagingFactory.read(stream);
             ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
             Document document = constructDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc");
             Envelope second = limitedPackagingFactory.addSignature(
                     envelope,
                     singletonList(document),
                     singletonList(stringEnvelopeAnnotation)
             )
        ) {
            Assert.assertNotNull(second);
            int contentCount = second.getSignatureContents().size();
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                envelopeWriter.write(second, bos);
                try (
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
                        Envelope inputEnvelope = limitedPackagingFactory.read(inputStream)) {
                    Assert.assertNotNull(inputEnvelope);
                    Assert.assertEquals(contentCount, inputEnvelope.getSignatureContents().size());
                    //Detach doc
                    inputEnvelope.getSignatureContents().get(1).detachDocument(
                            inputEnvelope.getSignatureContents().get(1)
                                    .getDocumentsManifest().getDocumentReferences().get(0).getUri(), documentFactory);
                    //Attach doc
                    inputEnvelope.getSignatureContents().get(1).attachDetachedDocument(
                            inputEnvelope.getSignatureContents().get(1)
                                    .getDocumentsManifest().getDocumentReferences().get(0).getUri(),
                            inputEnvelope.getSignatureContents().get(0)
                                    .getManifest().getInputStream(),
                            documentFactory
                    );
                    try {
                        writeEnvelopeToAndReadFromStream(inputEnvelope, limitedPackagingFactory);
                    } catch (EnvelopeReadingException e) {
                        //As expected.
                    }
                }
            }
        }
    }

    @Test
    public void testAddInternalDocumentToEnvelopeWithEmptyDocument_OK() throws Exception {
        try (Envelope envelope = getEnvelope(ENVELOPE_WITH_NO_DOCUMENTS)) {
            Manifest man = envelope.getSignatureContents().get(0).getManifest();
            addContentAndVerify(limitedPackagingFactory, envelope, man);
        }
    }

    @Test
    public void testEmptyDocumentAsReferredDocument_NOK() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Created envelope did not pass internal verification");
        try (Envelope envelope = getEnvelope(ENVELOPE_WITH_NO_DOCUMENTS)) {
            Document doc = envelope.getSignatureContents().get(0).getDocuments().values().iterator().next();
            addContentAndVerify(envelope, doc);
        }
    }

    @Test
    public void testAnnotationAsReferredDocument_OK() throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("randomData".getBytes());
             Document doc = constructDocument(inputStream, "application/random", "someFile.file");
             Envelope envelope = packagingFactory.create(singletonList(doc), singletonList(stringEnvelopeAnnotation))) {
            Annotation annotation = envelope.getSignatureContents().get(0).getAnnotations().values().iterator().next();
            addContentAndVerify(envelope, annotation);
        }
    }

    @Test
    public void testAnnotationManifestAsReferredDocument_OK() throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("randomData".getBytes());
             Document doc = constructDocument(inputStream, "application/random", "someFile.file");
             Envelope envelope = packagingFactory.create(singletonList(doc), singletonList(stringEnvelopeAnnotation))) {
            SingleAnnotationManifest annotationManifest =
                    envelope.getSignatureContents().get(0).getSingleAnnotationManifests().values().iterator().next();
            addContentAndVerify(envelope, annotationManifest);
        }
    }

    @Test
    public void testAnnotationManifestAsReferredDocumentWhileAnnotationDataMissing_OK() throws Exception {
        try (FileInputStream fis = new FileInputStream(loadFile(ENVELOPE_WITH_MISSING_ANNOTATION_DATA));
             Envelope envelope = packagingFactory.read(fis)) {
            SingleAnnotationManifest annotationManifest =
                    envelope.getSignatureContents().get(0).getSingleAnnotationManifests().values().iterator().next();
            addContentAndVerify(envelope, annotationManifest);
        }
    }

    @Test
    public void testCreateSignatureWithSameAnnotationInTwoContents() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            Assert.assertEquals(2, envelope.getSignatureContents().size());
            for (SignatureContent content : envelope.getSignatureContents()) {
                checkEnvelopeAnnotation(content);
            }
        }
    }

    @Test
    public void testReadingEnvelopeWithSameAnnotationInMultipleSignatures() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
            try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                SignatureContent firstContent = parsedEnvelope.getSignatureContents().get(0);
                SignatureContent secondContent = parsedEnvelope.getSignatureContents().get(1);
                Assert.assertEquals(
                        stringEnvelopeAnnotation,
                        firstContent.getAnnotations().values().iterator().next()
                );
                Assert.assertEquals(
                        firstContent.getAnnotations().values().iterator().next(),
                        secondContent.getAnnotations().values().iterator().next()
                );
            }
        }
    }

    @Test
    public void testWritingSplitEnvelopeWithSameAnnotationInMultipleSignatures() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation);
             Envelope first = new Envelope(envelope.getSignatureContents().get(0));
             Envelope second = new Envelope(envelope.getSignatureContents().get(1))) {

            Envelope[] arr = new Envelope[]{first, second};
            for (Envelope e : arr) {
                checkEnvelopeWithOneContent(e);
            }
        }
    }

    @Test
    public void testWritingAndReadingEnvelopeWithSameAnnotationInMultipleSignatures() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
            try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                Assert.assertNotNull(parsedEnvelope);
                Assert.assertEquals(2, parsedEnvelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testDeepCopyEnvelopeClosingOriginalDoesNotCloseCopy() throws Exception {
        Envelope copy = null;
        try (Envelope original = getEnvelope()) {
            // create copy
            copy = new Envelope(original, parsingStore);
        }
        EnvelopeWriter writer = new ZipEnvelopeWriter();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(copy, bos);
        copy.close();

        try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            Assert.assertEquals(1, parsedEnvelope.getSignatureContents().size());
        }
    }

    @Test
    public void testDeepCopyEnvelopeClosingOriginalAndCopyClearsParseStore() throws Exception {
        int tempFileCount = getKsieTempFiles().size();
        Envelope copy = null;
        try (Envelope original = getEnvelope()) {
            copy = new Envelope(original, new TemporaryFileBasedParsingStore());
        }

        Assert.assertTrue(tempFileCount < getKsieTempFiles().size());

        copy.close();

        Assert.assertEquals(tempFileCount, getKsieTempFiles().size());
    }

    @Test
    public void testDeepCopyEnvelopesAreEqual() throws Exception {
        // Testing equality with what they would be written to disk as to negate any locally implemented equals() bugs
        Envelope copy = null;
        EnvelopeWriter writer = new ZipEnvelopeWriter();
        byte[] originalContent = null;
        try (Envelope original = getEnvelope();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()
         ) {
            writer.write(original, bos);
            originalContent = bos.toByteArray();
            copy = new Envelope(original, parsingStore);

        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(copy, bos);
        copy.close();
        byte[] copyContent = bos.toByteArray();

        Assert.assertTrue(Arrays.equals(originalContent, copyContent));
    }

    private void addContentAndVerify(Envelope envelope, EnvelopeElement element) throws Exception {
        addContentAndVerify(packagingFactory, envelope, element);
    }

    private void addContentAndVerify(EnvelopePackagingFactory packagingFactory, Envelope envelope, EnvelopeElement element)
            throws Exception {
        Document doc2 = documentFactory.create(element);
        try (Envelope second = packagingFactory.addSignature(
                envelope,
                singletonList(doc2),
                Collections.singletonList(stringEnvelopeAnnotation)
        )) {
            Assert.assertEquals(2, second.getSignatureContents().size());
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(second, bos);

            try (Envelope envelope1 = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                Assert.assertEquals(2, envelope1.getSignatureContents().size());
            }
        }
    }

    private boolean compareSignatureContentListOrder(Envelope envelope) {
        SignatureContent previous = null;
        for (SignatureContent content : envelope.getSignatureContents()) {
            if (previous != null && !((KSISignature) previous.getEnvelopeSignature().getSignature()).getAggregationTime().before(
                    ((KSISignature) content.getEnvelopeSignature().getSignature()).getAggregationTime())) {
                return false;
            }
            previous = content;
        }
        return true;
    }

    private void checkEnvelopeWithOneContent(Envelope envelope) throws Exception {
        EnvelopeWriter writer = new ZipEnvelopeWriter();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(envelope, bos);
        try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
            Assert.assertEquals(1, parsedEnvelope.getSignatureContents().size());
            SignatureContent signatureContent = parsedEnvelope.getSignatureContents().get(0);
            checkEnvelopeAnnotation(signatureContent);
        }
    }


    private void checkEnvelopeAnnotation(SignatureContent content) {
        Assert.assertEquals(1, content.getAnnotations().size());
        Assert.assertEquals(1, content.getSingleAnnotationManifests().size());
        Assert.assertEquals(stringEnvelopeAnnotation, content.getAnnotations().values().iterator().next());
    }

    private Document constructDocument(ByteArrayInputStream input, String mimeTypeApplicationTxt, String s) {
        return documentFactory.create(input, mimeTypeApplicationTxt, s);
    }

}
