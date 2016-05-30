package com.guardtime.container.verification;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.AnnotationsIntegrityRule;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AnnotationsIntegrityRuleTest extends AbstractContainerTest {
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_ANNOTATION = "verification/annotations/container-with-fully-removable-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/container-with-fully-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/container-with-fully-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/container-with-fully-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/container-with-fully-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_ANNOTATION = "verification/annotations/container-with-value-removable-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/container-with-value-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/container-with-value-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/container-with-value-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/container-with-value-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_ANNOTATION = "verification/annotations/container-with-non-removable-annotation.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/container-with-non-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/container-with-non-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/container-with-non-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/container-with-non-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_ANNOTATIONS_MANIFEST = "verification/annotations/container-with-annotmanifest.ksie";
    private static final String CONTAINER_WITH_MISSING_ANNOTATIONS_MANIFEST = "verification/annotations/container-with-missing-annotmanifest.ksie";
    private static final String CONTAINER_WITH_CORRUPT_ANNOTATIONS_MANIFEST = "verification/annotations/container-with-corrupt-annotmanifest.ksie";

    @Mock
    private KSISignature mockKsiSignature;

    @Mock
    private SingleAnnotationManifest mockSingleAnnotationManifest;

    @Mock
    private SignatureContent mockSignatureContent;

    @Mock
    private com.guardtime.container.manifest.AnnotationDataReference mockAnnotationReference;

    private ContainerPackagingFactory packagingFactory;
    private Rule rule = new AnnotationsIntegrityRule();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(new KsiContainerSignature(mockKsiSignature));

        this.packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, new TlvContainerManifestFactory());
    }

    private RuleVerificationResult getRuleVerificationResult(String path) throws Exception {
        InputStream input = new FileInputStream(loadFile(path));
        Container container = packagingFactory.read(input);
        SignatureContent content = container.getSignatureContents().get(0);

        return selectMostImportantResult(rule.verify(content));
    }

    private RuleVerificationResult selectMostImportantResult(List<RuleVerificationResult> results) {
        RuleVerificationResult returnable = results.get(0);
        for (RuleVerificationResult result : results) {
            if (result.getResultStatus().isMoreImportantThan(returnable.getResultStatus())) {
                returnable = result;
            }
        }
        return returnable;
    }

    @Test
    public void testFullyRemovableAnnotationPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_FULLY_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testFullyRemovableAnnotationFullyRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testFullyRemovableAnnotationCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testFullyRemovableAnnotationDataRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testFullyRemovableAnnotationDataCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testValueRemovableAnnotationDataPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_VALUE_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testValueRemovableAnnotationDataRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testValueRemovableAnnotationDataCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testValueRemovableAnnotationRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testValueRemovableAnnotationCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testNonRemovableAnnotationDataPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_NON_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testNonRemovableAnnotationDataRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testNonRemovableAnnotationDataCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testNonRemovableAnnotationRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testNonRemovableAnnotationCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testAnnotationsManifestPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_ANNOTATIONS_MANIFEST);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testAnnotationsManifestRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_MISSING_ANNOTATIONS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testAnnotationsManifestCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_CORRUPT_ANNOTATIONS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }
}
