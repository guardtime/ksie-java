package com.guardtime.container.verification;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.DocumentsIntegrityRule;
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

public class DocumentsIntegrityRuleTest extends AbstractContainerTest {
    private static final String CONTAINER_WITH_DOCUMENTS_MANIFEST = "verification/documents/container-with-documents-manifest.ksie";
    private static final String CONTAINER_WITH_MISSING_DOCUMENTS_MANIFEST = "verification/documents/container-with-missing-documents-manifest.ksie";
    private static final String CONTAINER_WITH_CORRUPT_DOCUMENTS_MANIFEST = "verification/documents/container-with-corrupt-documents-manifest.ksie";
    private static final String CONTAINER_WITH_DOCUMENT= "verification/documents/container-with-document.ksie";
    private static final String CONTAINER_WITH_MISSING_DOCUMENT = "verification/documents/container-with-missing-document.ksie";
    private static final String CONTAINER_WITH_CORRUPT_DOCUMENT = "verification/documents/container-with-corrupt-document.ksie";

    @Mock
    private KSISignature mockKsiSignature;

    private ContainerPackagingFactory packagingFactory;
    private Rule rule = new DocumentsIntegrityRule();

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
    public void testDocumentsManifestPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_DOCUMENTS_MANIFEST);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testDocumentsManifestMissing_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_MISSING_DOCUMENTS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testDocumentsManifestCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_CORRUPT_DOCUMENTS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testDocumentPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_DOCUMENT);

        assertEquals(VerificationResult.OK, result.getResultStatus());
    }

    @Test
    public void testDocumentMissing_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_MISSING_DOCUMENT);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }

    @Test
    public void testDocumentCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_CORRUPT_DOCUMENT);

        assertEquals(VerificationResult.NOK, result.getResultStatus());
    }
}
