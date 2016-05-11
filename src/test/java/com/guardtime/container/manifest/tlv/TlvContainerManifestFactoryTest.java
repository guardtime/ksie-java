package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
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
    private Pair<String, TlvDataFilesManifest> mockedDataManifestPair;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockedAnnotationPair = Pair.of(MOCK_URI, mockAnnotation);
        mockedDataManifestPair = Pair.of(TEST_FILE_NAME_TEST_TXT, mockDataManifest);
    }

    @Test
    public void testCreateAnnotationInfoManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document manifest");
        factory.createAnnotationInfoManifest(null, mockedAnnotationPair);
    }

    @Test
    public void testCreateAnnotationInfoManifestWithoutAnnotation_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation");
        factory.createAnnotationInfoManifest(mockedDataManifestPair, null);
    }

    @Test
    public void testCreateAnnotationInfoManifest() throws Exception {
        TlvAnnotationInfoManifest manifest = factory.createAnnotationInfoManifest(mockedDataManifestPair, mockedAnnotationPair);
        assertNotNull(manifest);
        assertNotNull(manifest.getAnnotationReference());
        assertNotNull(manifest.getDataManifestReference());
    }

    @Test
    public void testCreateAnnotationsManifestWithoutAnnotationInfoManifestsOK() throws Exception {
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(new HashMap<String, Pair<ContainerAnnotation, TlvAnnotationInfoManifest>>());
        assertNotNull(manifest);
    }

    @Test
    public void testCreateAnnotationsManifestOK() throws Exception {
        Map<String, Pair<ContainerAnnotation, TlvAnnotationInfoManifest>> annotationManifests = new HashMap();
        annotationManifests.put("Non-important-for-test", Pair.of(mockAnnotation, mockAnnotationInfoManifest));
        TlvAnnotationsManifest manifest = factory.createAnnotationsManifest(annotationManifests);

        assertNotNull("Manifest was not created", manifest);
    }

    @Test
    public void testCreateDataFilesManifestWithEmptyDataFiles_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files list must not be empty");
        factory.createDataFilesManifest(new ArrayList<ContainerDocument>());
    }

    @Test
    public void testCreateDataFilesManifestWithoutDataFiles_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document list must be present");
        factory.createDataFilesManifest(null);
    }

    @Test
    public void testCreateDataFilesManifestOK() throws Exception {
        TlvDataFilesManifest manifest = factory.createDataFilesManifest(asList(TEST_DOCUMENT_HELLO_TEXT));
        assertNotNull("Manifest was not created", manifest);
        assertEquals(1, manifest.getDataFileReferences().size());
    }

    @Test
    public void testCreateSignatureManifestWithoutDataManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Document manifest must be present");
        factory.createSignatureManifest(null, Pair.of("Non-important-for-test", mockAnnotationsManifest), Pair.of("Non-important-for-test", "signature.ksig"));
    }

    @Test
    public void testCreateSignatureManifestWithoutAnnotationsManifest_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotations manifest must be present");
        factory.createSignatureManifest(Pair.of("Non-important-for-test", mockDataManifest), null, Pair.of("Non-important-for-test", "signature.ksig"));
    }

    @Test
    public void testCreateSignatureManifestOK() throws Exception {
        TlvSignatureManifest manifest = factory.createSignatureManifest(
                Pair.of("Non-important-for-test", mockDataManifest),
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