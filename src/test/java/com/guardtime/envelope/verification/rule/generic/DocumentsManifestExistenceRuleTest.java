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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DocumentsManifestExistenceRuleTest extends AbstractEnvelopeTest {

    private Rule rule = new DocumentsManifestExistenceRule(defaultRuleStateProvider);

    @Test
    public void testDocumentsManifestExistsResultsInOK() throws Exception {
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        String documentsManifestPath = "datamanifest.ext";
        when(mockFileReference.getUri()).thenReturn(documentsManifestPath);
        when(mockedManifest.getDocumentsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(mockedManifest);
        when(mockedDocumentsManifest.getPath()).thenReturn(documentsManifestPath);
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(mockedDocumentsManifest);

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.OK, holder.getAggregatedResult());
    }

    @Test
    public void testDocumentsManifestDoesNotExist_NOK() throws Exception {
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        String documentsManifestPath = "datamanifest.ext";
        when(mockFileReference.getUri()).thenReturn(documentsManifestPath);
        when(mockedManifest.getDocumentsManifestReference()).thenReturn(mockFileReference);
        when(mockSignatureContent.getManifest()).thenReturn(mockedManifest);
        when(mockSignatureContent.getDocumentsManifest()).thenReturn(null);

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        assertEquals(VerificationResult.NOK, holder.getAggregatedResult());
    }

}
