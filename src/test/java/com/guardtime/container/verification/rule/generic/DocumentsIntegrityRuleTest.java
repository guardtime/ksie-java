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

package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
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
    private static final String CONTAINER_WITH_MISSING_DOCUMENTS_MANIFEST = "verification/documents/container-with-missing-documents-manifest.ksie";
    private static final String CONTAINER_WITH_CORRUPT_DOCUMENTS_MANIFEST = "verification/documents/container-with-corrupt-documents-manifest.ksie";
    private static final String CONTAINER_WITH_DOCUMENT = "verification/documents/container-with-document.ksie";
    private static final String CONTAINER_WITH_MISSING_DOCUMENT = "verification/documents/container-with-missing-document.ksie";
    private static final String CONTAINER_WITH_CORRUPT_DOCUMENT = "verification/documents/container-with-corrupt-document.ksie";

    @Mock
    private KSISignature mockKsiSignature;

    private ContainerPackagingFactory packagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        ContainerSignature mockedContainerSignature = Mockito.mock(ContainerSignature.class);
        when(mockedContainerSignature.getSignature()).thenReturn(mockKsiSignature);
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedContainerSignature);

        this.packagingFactory = new ZipContainerPackagingFactoryBuilder().withSignatureFactory(mockedSignatureFactory).build();
    }

    private RuleVerificationResult getRuleVerificationResult(String path) throws Exception {
        InputStream input = new FileInputStream(loadFile(path));
        Container container = null;
        try {
            container = packagingFactory.read(input);
        } catch (ContainerReadingException e) {
            container = e.getContainer();
        }
        SignatureContent content = container.getSignatureContents().get(0);
        ResultHolder holder = new ResultHolder();
        new DocumentsManifestExistenceRule(defaultRuleStateProvider).verify(holder, content);
        new DocumentsManifestIntegrityRule(defaultRuleStateProvider).verify(holder, content);
        new DocumentExistenceRule(defaultRuleStateProvider).verify(holder, content);
        new DocumentIntegrityRule(defaultRuleStateProvider).verify(holder, content);
        container.close();
        return selectMostImportantResult(holder.getResults());
    }

    private RuleVerificationResult selectMostImportantResult(List<RuleVerificationResult> results) {
        if(results.isEmpty()) {
            return null;
        }
        RuleVerificationResult returnable = results.get(0);
        for (RuleVerificationResult result : results) {
            if (result.getVerificationResult().isMoreImportantThan(returnable.getVerificationResult())) {
                returnable = result;
            }
        }
        return returnable;
    }

    @Test
    public void testDocumentsManifestMissing_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_MISSING_DOCUMENTS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testDocumentsManifestCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_CORRUPT_DOCUMENTS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testDocumentPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_DOCUMENT);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testDocumentMissing_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_MISSING_DOCUMENT);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testDocumentCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResult(CONTAINER_WITH_CORRUPT_DOCUMENT);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }
}
