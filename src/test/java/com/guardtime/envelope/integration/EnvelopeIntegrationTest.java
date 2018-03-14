package com.guardtime.envelope.integration;

import com.guardtime.envelope.EnvelopeBuilder;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.InternalDocument;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.packaging.parsing.store.MemoryBasedParsingStoreFactory;
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
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class EnvelopeIntegrationTest extends AbstractCommonIntegrationTest {

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
                .withParsingStoreFactory(new MemoryBasedParsingStoreFactory())
                .build();

        expectedException.expect(SignatureException.class);
        expectedException.expectMessage("HTTP request failed");
        try (
                Envelope ignored = new EnvelopeBuilder(factory)
                        .withDocument(
                                new ByteArrayInputStream("Test_Data".getBytes(StandardCharsets.UTF_8)),
                                TEST_FILE_NAME_TEST_TXT,
                                "application/txt"
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
                Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));

            packagingFactory.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );

            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));
        }
    }

    @Test
    public void testContentOrderWithThreeContents() throws Exception {
        try (
                FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MIXED_INDEX_TYPES_IN_CONTENTS));
                Envelope existingEnvelope = packagingFactory.read(stream);
                ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
                Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));

            packagingFactory.addSignature(
                    existingEnvelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );

            Assert.assertTrue(compareSignatureContentListOrder(existingEnvelope));
        }
    }

    @Test
    public void testDocumentOrderWithSameSigningTimes() throws Exception {
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_MULTIPLE_SIGNATURES_WITH_SAME_SIGNING_TIME));
             Envelope envelope = packagingFactory.read(stream);
             ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
             Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            packagingFactory.addSignature(
                    envelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );
            KSISignature signature0 = (KSISignature) envelope.getSignatureContents().get(0).getEnvelopeSignature().getSignature();
            KSISignature signature1 = (KSISignature) envelope.getSignatureContents().get(1).getEnvelopeSignature().getSignature();
            KSISignature signature2 = (KSISignature) envelope.getSignatureContents().get(2).getEnvelopeSignature().getSignature();

            Assert.assertFalse(signature0.getAggregationTime().after(signature1.getAggregationTime()));
            Assert.assertFalse(signature0.getAggregationTime().before(signature1.getAggregationTime()));

            Assert.assertTrue(signature2.getAggregationTime().after(signature0.getAggregationTime()));
            Assert.assertTrue(signature2.getAggregationTime().after(signature1.getAggregationTime()));

            Assert.assertTrue(envelope.getSignatureContents().get(0).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-1.ksi"));
            Assert.assertTrue(envelope.getSignatureContents().get(1).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-2.ksi"));
            Assert.assertTrue(envelope.getSignatureContents().get(2).getManifest().getSignatureReference().getUri()
                    .startsWith("META-INF/signature-3.ksi"));
        }
    }

    @Test
    public void testInternalFileReferencedAsDocument() throws  Exception {
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_INTERNAL_FILE_AS_DOC_REFERENCE));
             Envelope envelope = packagingFactory.read(stream)
        ) {
            SignatureContent content = envelope.getSignatureContents().get(1);
            content.detachDocument(content.getDocuments().values().iterator().next().getFileName());

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
        EnvelopePackagingFactory factory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withIndexProviderFactory(new IncrementingIndexProviderFactory())
                .withParsingStoreFactory(new MemoryBasedParsingStoreFactory())
                .withVerificationPolicy(new LimitedInternalVerificationPolicy())
                .build();
        try (FileInputStream stream = new FileInputStream(loadFile(ENVELOPE_WITH_UNUSED_INTERNAL_FILE_AS_DOC_REFERENCE));
             Envelope envelope = factory.read(stream);
             ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA_TXT_CONTENT);
             Document document = new StreamDocument(input, MIME_TYPE_APPLICATION_TXT, "Doc.doc")
        ) {
            factory.addSignature(
                    envelope,
                    singletonList(document),
                    singletonList(stringEnvelopeAnnotation)
            );

            assertNotNull(envelope);
            int contentCount = envelope.getSignatureContents().size();
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                envelopeWriter.write(envelope, bos);
                try (
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
                        Envelope inputEnvelope = factory.read(inputStream)) {
                    assertNotNull(inputEnvelope);
                    Assert.assertEquals(contentCount, inputEnvelope.getSignatureContents().size());
                    //Detach doc
                    inputEnvelope.getSignatureContents().get(1).detachDocument(
                            inputEnvelope.getSignatureContents().get(1)
                                    .getDocumentsManifest().getDocumentReferences().get(0).getUri());
                    //Attach doc
                    inputEnvelope.getSignatureContents().get(1).attachDetachedDocument(
                            inputEnvelope.getSignatureContents().get(1)
                                    .getDocumentsManifest().getDocumentReferences().get(0).getUri(),
                            inputEnvelope.getSignatureContents().get(0)
                                    .getManifest().getInputStream()
                    );
                    try {
                        writeEnvelopeToAndReadFromStream(inputEnvelope, factory);
                    } catch (EnvelopeReadingException e) {
                        //As expected.
                    }
                }
            }
        }
    }

    @Test
    public void testAnnotationAsReferredDocument_OK() throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("randomData".getBytes());
             Document doc = new StreamDocument(inputStream, "application/random", "someFile.file");
             Envelope envelope = packagingFactory.create(singletonList(doc), singletonList(stringEnvelopeAnnotation))) {
            Annotation annotation = envelope.getSignatureContents().get(0).getAnnotations().values().iterator().next();
            Document doc2 = new InternalDocument(annotation);
            packagingFactory.addSignature(envelope, singletonList(doc2), Collections.<Annotation>emptyList());
            assertEquals(2, envelope.getSignatureContents().size());
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);

            try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                assertEquals(2, parsedEnvelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testRemovingSignatureContentWithSameAnnotationDoesNotChangeAnnotation() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            SignatureContent firstContent = envelope.getSignatureContents().get(0);
            SignatureContent secondContent = envelope.getSignatureContents().get(1);

            envelope.removeSignatureContent(firstContent);
            assertFalse(firstContent.getAnnotations().isEmpty());
            assertFalse(secondContent.getAnnotations().isEmpty());
            assertEquals(
                    stringEnvelopeAnnotation,
                    firstContent.getAnnotations().values().iterator().next()
            );
            assertEquals(
                    firstContent.getAnnotations().values().iterator().next(),
                    secondContent.getAnnotations().values().iterator().next()
            );
        }
    }

    @Test
    public void testWritingSplitEnvelopeWithSameAnnotationInMultipleSignatures() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            SignatureContent firstContent = envelope.getSignatureContents().get(0);
            envelope.removeSignatureContent(firstContent);
            Envelope[] arr = new Envelope[]{envelope, new Envelope(firstContent)};
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (Envelope e : arr) {
                bos.reset();
                writer.write(e, bos);
                try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                    assertEquals(1, parsedEnvelope.getSignatureContents().size());
                    SignatureContent signatureContent = parsedEnvelope.getSignatureContents().get(0);
                    assertEquals(1, signatureContent.getAnnotations().size());
                    assertEquals(1, signatureContent.getSingleAnnotationManifests().size());
                    assertEquals(stringEnvelopeAnnotation, signatureContent.getAnnotations().values().iterator().next());
                }
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
                assertEquals(
                        stringEnvelopeAnnotation,
                        firstContent.getAnnotations().values().iterator().next()
                );
                assertEquals(
                        firstContent.getAnnotations().values().iterator().next(),
                        secondContent.getAnnotations().values().iterator().next()
                );
            }
        }
    }


    @Test
    public void testReadingEnvelopeWithSameAnnotationInMultipleSignaturesAndRemovingOneAndWriting() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
            try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                SignatureContent firstContent = parsedEnvelope.getSignatureContents().get(0);
                parsedEnvelope.removeSignatureContent(firstContent);
                firstContent.close();
                bos.reset();
                writer.write(parsedEnvelope, bos);
                try (Envelope reParsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                    assertEquals(1, reParsedEnvelope.getSignatureContents().size());
                    SignatureContent signatureContent = reParsedEnvelope.getSignatureContents().get(0);
                    assertEquals(1, signatureContent.getAnnotations().size());
                    assertEquals(1, signatureContent.getSingleAnnotationManifests().size());
                    assertEquals(stringEnvelopeAnnotation, signatureContent.getAnnotations().values().iterator().next());
                }
            }
        }
    }

    @Test
    public void testRemovingSignatureFromEnvelopeWithSameAnnotationInMultipleSignaturesAndWriting() throws Exception {
        try (Envelope envelope =
                     getEnvelopeWith2SignaturesWithSameAnnotation(stringEnvelopeAnnotation)) {
            SignatureContent firstContent = envelope.getSignatureContents().get(0);
            envelope.removeSignatureContent(firstContent);
            firstContent.close();
            EnvelopeWriter writer = new ZipEnvelopeWriter();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(envelope, bos);
            try (Envelope parsedEnvelope = packagingFactory.read(new ByteArrayInputStream(bos.toByteArray()))) {
                assertEquals(1, parsedEnvelope.getSignatureContents().size());
                SignatureContent signatureContent = parsedEnvelope.getSignatureContents().get(0);
                assertEquals(1, signatureContent.getAnnotations().size());
                assertEquals(1, signatureContent.getSingleAnnotationManifests().size());
                assertEquals(stringEnvelopeAnnotation, signatureContent.getAnnotations().values().iterator().next());
            }
        }
    }

    private Envelope getEnvelopeWith2SignaturesWithSameAnnotation(Annotation annotation) throws InvalidEnvelopeException,
            SignatureException,
            EnvelopeMergingException {
        Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(annotation)
        );
        packagingFactory.addSignature(
                envelope,
                singletonList(testDocumentHelloPdf),
                singletonList(annotation)
        );
        return envelope;
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

}
