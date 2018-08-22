/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.integration;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStore;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.packaging.zip.ZipEnvelopeWriter;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.pdu.PduVersion;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.HttpClientSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpClient;
import com.guardtime.ksi.trust.X509CertificateSubjectRdnSelector;

import org.junit.Assert;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class AbstractCommonIntegrationTest extends AbstractEnvelopeTest {

    private static final File TRUST_STORE_FILE;
    private static final String TRUST_STORE_PASSWORD;
    protected EnvelopePackagingFactory packagingFactory;
    protected EnvelopePackagingFactory packagingFactoryTFPS; // TemporaryFileParsingStore
    protected SignatureFactory signatureFactory;
    protected EnvelopeWriter envelopeWriter;
    protected KSI ksi;

    protected Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

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
            KSI_SERVICE_CREDENTIALS = new KSIServiceCredentials(
                    properties.getProperty("credentials.id"),
                    properties.getProperty("credentials.key")
            );
            TRUST_STORE_FILE = new File(
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResource("ksi-truststore.jks")
                            .toURI()
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
                KSI_SERVICE_CREDENTIALS,
                PduVersion.V2
        );
        SimpleHttpClient httpClient = new SimpleHttpClient(settings);
        ksi = new KSIBuilder()
                .setKsiProtocolSignerClient(httpClient)
                .setKsiProtocolExtenderClient(httpClient)
                .setKsiProtocolPublicationsFileClient(httpClient)
                .setPublicationsFileTrustedCertSelector(new X509CertificateSubjectRdnSelector("E=publications@guardtime.com"))
                .setPublicationsFilePkiTrustStore(TRUST_STORE_FILE, TRUST_STORE_PASSWORD)
                .build();
        signatureFactory = new KsiSignatureFactory(ksi, ksi);
        packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .build();
        packagingFactoryTFPS = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withParsingStore(TemporaryFileBasedParsingStore.getInstance())
                .build();
        envelopeWriter = new ZipEnvelopeWriter();
    }

    Envelope getEnvelope() throws Exception {
        return getEnvelope(ENVELOPE_WITH_ONE_DOCUMENT);
    }

    Envelope getEnvelope(String path) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = new File(url.toURI());
        try (FileInputStream input = new FileInputStream(file)) {
            return packagingFactory.read(input);
        }
    }

    Envelope getEnvelopeIgnoreExceptions(String path) throws Exception {
        try {
            return getEnvelope(path);
        } catch (EnvelopeReadingException e) {
            return e.getEnvelope();
        }
    }

    Envelope getEnvelopeWithTemporaryFileParsingStore(String path) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = new File(url.toURI());
        try (FileInputStream input = new FileInputStream(file)) {
            return packagingFactoryTFPS.read(input);
        }
    }

    /*
    Created envelope will be closed.
     */
    void writeEnvelopeToAndReadFromStream(Envelope envelope, EnvelopePackagingFactory packagingFactory)
            throws Exception {
        Assert.assertNotNull(envelope);
        int contentCount = envelope.getSignatureContents().size();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            envelopeWriter.write(envelope, bos);
            try (
                    ByteArrayInputStream stream = new ByteArrayInputStream(bos.toByteArray());
                    Envelope inputEnvelope = packagingFactory.read(stream)) {
                Assert.assertNotNull(inputEnvelope);
                Assert.assertEquals(contentCount, inputEnvelope.getSignatureContents().size());
            }
        }
    }

    Envelope getEnvelopeWith2SignaturesWithSameAnnotation(Annotation annotation) throws Exception {
        Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloText),
                singletonList(annotation)
        );
        Envelope second = packagingFactory.addSignature(
                envelope,
                singletonList(testDocumentHelloPdf),
                singletonList(annotation)
        );
        envelope.close();
        return second;
    }


    protected boolean anyKsieTempFiles() {
        return getKsieTempFiles().size() > 0;
    }

    protected List<File> getKsieTempFiles() {
        List<File> ksieTempFiles = new ArrayList<>();
        for (File f : tempFiles()) {
            if (isTempFile(f)) {
                ksieTempFiles.add(f);
            } else if (isTempDir(f)) {
                ksieTempFiles.addAll(getKsieTempFilesFromDirectory(f));
            }
        }
        return ksieTempFiles;
    }

    protected List<File> getKsieTempFilesFromDirectory(File dir) {
        List<File> ksieTempFiles = new ArrayList<>();
        for (File f : tempFiles(dir)) {
            if (isTempFile(f)) {
                ksieTempFiles.add(f);
            } else if (isTempDir(f)) {
                ksieTempFiles.addAll(getKsieTempFilesFromDirectory(f));
            }
        }
        return ksieTempFiles;
    }

    protected void cleanTempDir() throws IOException {
        for (File f : tempFiles()) {
            if (isTempFile(f) || isTempDir(f)) {
                Util.deleteFileOrDirectory(f.toPath());
            }
        }
    }

    protected List<File> tempFiles() {
        return tempFiles(tmpDir.toFile());
    }

    protected List<File> tempFiles(File dir) {
        File[] list = dir.listFiles();
        if (list == null) {
            return emptyList();
        } else {
            return asList(list);
        }
    }

    protected boolean isTempFile(File s) {
        return s.isFile() && s.getName().startsWith(Util.TEMP_FILE_PREFIX);
    }

    protected boolean isTempDir(File s) {
        return s.getName().startsWith(Util.TEMP_DIR_PREFIX) && s.isDirectory();
    }
}
