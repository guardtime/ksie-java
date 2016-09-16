package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.util.Pair;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TlvContainerManifestFactoryTest extends AbstractTlvManifestTest {

    private TlvContainerManifestFactory factory = new TlvContainerManifestFactory();
    private Pair<String, ContainerAnnotation> mockedAnnotationPair;
    private Pair<String, TlvDocumentsManifest> mockedDocumentsManifestPair;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockedAnnotationPair = Pair.of(MOCK_URI, mockAnnotation);
        mockedDocumentsManifestPair = Pair.of(TEST_FILE_NAME_TEST_TXT, mockDocumentsManifest);
    }

    @Test
    public void testCreateTlvContainerManifestFactoryWithoutHashAlgorithm_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm provider");
        new TlvContainerManifestFactory(null);
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutDocumentsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Documents manifest");
        factory.createSingleAnnotationManifest(null, mockedAnnotationPair);
    }

    @Test
    public void testCreateSingleAnnotationManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation");
        factory.createSingleAnnotationManifest(mockedDocumentsManifestPair, null);
    }

    @Test
    public void testCreateSingleAnnotationManifest() throws Exception {
        TlvSingleAnnotationManifest singleAnnotationManifest = factory.createSingleAnnotationManifest(mockedDocumentsManifestPair, mockedAnnotationPair);
        assertNotNull(singleAnnotationManifest);
        assertNotNull(singleAnnotationManifest.getAnnotationReference());
        assertNotNull(singleAnnotationManifest.getDocumentsManifestReference());
    }

    @Test
    public void testCreateAnnotationsManifestWithoutSingleAnnotationManifestsOK() throws Exception {
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(new HashMap<String, Pair<ContainerAnnotation, TlvSingleAnnotationManifest>>());
        assertNotNull(manifest);
    }

    @Test
    public void testCreateAnnotationsManifestOK() throws Exception {
        Map<String, Pair<ContainerAnnotation, TlvSingleAnnotationManifest>> annotationManifests = new HashMap();
        annotationManifests.put("Non-important-for-test", Pair.of(mockAnnotation, mockSingleAnnotationManifest));
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(annotationManifests);

        assertNotNull("Manifest was not created", manifest);
    }

    @Test
    public void testCreateDocumentsManifestWithEmptyDocumentsList_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files list must not be empty");
        factory.createDocumentsManifest(new ArrayList<ContainerDocument>());
    }

    @Test
    public void testCreateDocumentsManifestWithoutDocumentsList_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document files list must be present");
        factory.createDocumentsManifest(null);
    }

    @Test
    public void testCreateDocumentsManifestOK() throws Exception {
        TlvDocumentsManifest documentsManifest = factory.createDocumentsManifest(asList(TEST_DOCUMENT_HELLO_TEXT));
        assertNotNull("Manifest was not created", documentsManifest);
        assertEquals(1, documentsManifest.getDocumentReferences().size());
    }

    @Test
    public void testCreateManifestWithoutDocumentsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Documents manifest must be present");
        factory.createManifest(null, Pair.of("Non-important-for-test", mockAnnotationsManifest), Pair.of("Non-important-for-test", "signature.ksig"));
    }

    @Test
    public void testCreateManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotations manifest must be present");
        factory.createManifest(Pair.of("Non-important-for-test", mockDocumentsManifest), null, Pair.of("Non-important-for-test", "signature.ksig"));
    }

    @Test
    public void testCreateManifestOK() throws Exception {
        TlvManifest manifest = factory.createManifest(
                Pair.of("Non-important-for-test", mockDocumentsManifest),
                Pair.of("Non-important-for-test", mockAnnotationsManifest),
                Pair.of("Non-important-for-test", "signature.ksig")
        );

        assertNotNull("Manifest was not created", manifest);
    }

    @Test
    public void testGetManifestFactoryType() throws Exception {
        TlvManifestFactoryType type = factory.getManifestFactoryType();
        assertNotNull(type);
        assertNotNull(type.getManifestFileExtension());
        assertNotNull(type.getName());
    }

}