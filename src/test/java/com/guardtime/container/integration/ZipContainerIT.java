package com.guardtime.container.integration;


import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.context.SimpleVerificationContext;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.ksi.KsiPolicyBasedSignatureIntegrityRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZipContainerIT extends AbstractCommonIntegrationTest{

    private static final String TEST_FILE_NAME = "test.txt";

    @Test
    public void testCreateContainer() throws Exception {
        Container container = new ContainerBuilder(packagingFactory)
                .withDataFile(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME, "application/txt")
                .build();
        assertSingleContentsWithSingleDocumentWithName(container, TEST_FILE_NAME);
    }

    @Test
    public void testReadContainer() throws Exception {
        Container container = packagingFactory.read(Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_ONE_DOCUMENT).toURI())));
        assertSingleContentsWithSingleDocument(container);
    }

    private void assertSingleContentsWithSingleDocumentWithName(Container container, String testFileName) {
        List<? extends SignatureContent> contents = container.getSignatureContents();
        assertNotNull(contents);
        assertEquals(1, contents.size());

        SignatureContent content = contents.get(0);
        assertNotNull(content);
        Map<String, ContainerDocument> documents = content.getDocuments();
        assertEquals(1, documents.size());
        if(testFileName != null) {
            assertNotNull(documents.get(testFileName));
        }
    }

    private void assertSingleContentsWithSingleDocument(Container container) {
        assertSingleContentsWithSingleDocumentWithName(container, null);
    }

}
