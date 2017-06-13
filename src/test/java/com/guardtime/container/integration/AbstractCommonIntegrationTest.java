/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.integration;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;

import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public abstract class AbstractCommonIntegrationTest extends AbstractContainerTest {

    private static final File TRUST_STORE_FILE;
    private static final String TRUST_STORE_PASSWORD;
    protected ContainerPackagingFactory packagingFactory;
    protected SignatureFactory signatureFactory;
    protected KSI ksi;

    private static final KSIServiceCredentials KSI_SERVICE_CREDENTIALS;
    private static final String TEST_SIGNING_SERVICE;
    private static final String TEST_EXTENDING_SERVICE;

    private static final String GUARDTIME_PUBLICATIONS_FILE;

    static {
        try {
            Properties properties = new Properties();
            URL url = Thread.currentThread().getContextClassLoader().getResource("config.properties");
            properties.load(new FileInputStream(new File(url.toURI())));
            TEST_SIGNING_SERVICE = properties.getProperty("service.signing");
            TEST_EXTENDING_SERVICE = properties.getProperty("service.extending");
            GUARDTIME_PUBLICATIONS_FILE = properties.getProperty("publications.file.url");
            KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials(properties.getProperty("credentials.id"), properties.getProperty("credentials.key"));
            TRUST_STORE_FILE = new File(
                    Thread.currentThread().
                            getContextClassLoader().
                            getResource("ksi-truststore.jks").
                            toURI()
            );
            TRUST_STORE_PASSWORD = "changeit";
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                .setPublicationsFilePkiTrustStore(TRUST_STORE_FILE, TRUST_STORE_PASSWORD)
                .build();
        signatureFactory = new KsiSignatureFactory(ksi);
        packagingFactory = new ZipContainerPackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .enableInternalVerification()
                .build();

    }

    Container getContainer() throws Exception {
        return getContainer(CONTAINER_WITH_ONE_DOCUMENT);
    }

    Container getContainer(String container) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(container);
        File file = new File(url.toURI());
        try (FileInputStream input = new FileInputStream(file)) {
            return packagingFactory.read(input);
        }
    }

    Container getContainerIgnoreExceptions(String container) throws Exception {
        try {
            return getContainer(container);
        } catch (ContainerReadingException e) {
            return e.getContainer();
        }
    }

}
