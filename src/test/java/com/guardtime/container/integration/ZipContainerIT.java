package com.guardtime.container.integration;


import com.guardtime.container.ContainerBuilder;
import com.guardtime.container.document.ContainerDocument;
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

public class ZipContainerIT {

    private static final String TEST_FILE_NAME = "test.txt";
    private static final String CONTAINER_WITH_ONE_FILE = "containers/container-one-file.ksie";
    private static final String TEST_SIGNING_SERVICE = "http://ksigw.test.guardtime.com:3333/gt-signingservice";
    private static final String TEST_EXTENDING_SERVICE = "http://ksigw.test.guardtime.com:8010/gt-extendingservice";
    private static final String GUARDTIME_PUBLICATIONS_FILE = "http://verify.guardtime.com/ksi-publications.bin";
    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials("anon", "anon");
    private ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
    private ZipContainerPackagingFactory packagingFactory;
    private SignatureFactory signatureFactory;
    private KSI ksi;

    @Before
    public void setUp() throws Exception {
        HttpClientSettings settings = new HttpClientSettings(
                TEST_SIGNING_SERVICE,
                TEST_EXTENDING_SERVICE,
                GUARDTIME_PUBLICATIONS_FILE,
                KSI_SERVICE_CREDENTIALS
        );
        SimpleHttpClient httpClient = new SimpleHttpClient(settings);
        ksi = new KSIBuilder()
                .setKsiProtocolSignerClient(httpClient)
                .setKsiProtocolExtenderClient(httpClient)
                .setKsiProtocolPublicationsFileClient(httpClient)
                .setPublicationsFileTrustedCertSelector(new X509CertificateSubjectRdnSelector("E=publications@guardtime.com"))
                .build();
        signatureFactory = new KsiSignatureFactory(ksi);
        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
    }

    @Test
    public void testCreateContainer() throws Exception {
        Container container = new ContainerBuilder(packagingFactory)
                .withDocument(new ByteArrayInputStream("Test_Data".getBytes()), TEST_FILE_NAME, "application/txt")
                .build();
        assertSingleContentsWithSingleDocumentWithName(container, TEST_FILE_NAME);
        assertContainerVerifiesWithResult(container, RuleResult.OK);
    }

    @Test
    public void testReadContainer() throws Exception {
        Container container = packagingFactory.read(Files.newInputStream(Paths.get(ClassLoader.getSystemResource(CONTAINER_WITH_ONE_FILE).toURI())));
        assertSingleContentsWithSingleDocument(container);
        assertContainerVerifiesWithResult(container, RuleResult.OK);
    }


    private void assertContainerVerifiesWithResult(Container container, RuleResult expected) {
        ContainerVerifier verifier = new ContainerVerifier(new DefaultVerificationPolicy(getExtraRules()));
        VerifierResult result = verifier.verify(new SimpleVerificationContext(container));
        assertEquals(expected, result.getVerificationResult());
    }

    private LinkedList<Rule> getExtraRules() {
        LinkedList<Rule> extraRules = new LinkedList<>();
        extraRules.add(new KsiPolicyBasedSignatureIntegrityRule(ksi, new KeyBasedVerificationPolicy()));
        extraRules.add(new MimeTypeIntegrityRule(packagingFactory));
        return extraRules;
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
