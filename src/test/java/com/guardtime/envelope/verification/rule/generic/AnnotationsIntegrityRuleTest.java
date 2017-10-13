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
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
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

public class AnnotationsIntegrityRuleTest extends AbstractEnvelopeTest {
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_ANNOTATION = "verification/annotations/envelope-with-fully-removable-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/envelope-with-fully-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/envelope-with-fully-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/envelope-with-fully-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/envelope-with-fully-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_ANNOTATION = "verification/annotations/envelope-with-value-removable-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/envelope-with-value-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/envelope-with-value-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/envelope-with-value-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/envelope-with-value-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_ANNOTATION = "verification/annotations/envelope-with-non-removable-annotation.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION_DATA = "verification/annotations/envelope-with-non-removable-missing-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION_DATA = "verification/annotations/envelope-with-non-removable-corrupt-annotation-data.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION = "verification/annotations/envelope-with-non-removable-missing-annotation.ksie";
    private static final String CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION = "verification/annotations/envelope-with-non-removable-corrupt-annotation.ksie";
    private static final String CONTAINER_WITH_MISSING_ANNOTATIONS_MANIFEST = "verification/annotations/envelope-with-missing-annotmanifest.ksie";
    private static final String CONTAINER_WITH_CORRUPT_ANNOTATIONS_MANIFEST = "verification/annotations/envelope-with-corrupt-annotmanifest.ksie";

    @Mock
    private KSISignature mockKsiSignature;

    private EnvelopePackagingFactory packagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        EnvelopeSignature mockedEnvelopeSignature = Mockito.mock(EnvelopeSignature.class);
        when(mockedEnvelopeSignature.getSignature()).thenReturn(mockKsiSignature);
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedEnvelopeSignature);

        this.packagingFactory = new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(mockedSignatureFactory).build();
    }

    private RuleVerificationResult getRuleVerificationResults(String path) throws Exception {
        InputStream input = new FileInputStream(loadFile(path));
        Envelope envelope;
        try {
            envelope = packagingFactory.read(input);
        } catch (EnvelopeReadingException e) {
            envelope = e.getEnvelope();
        }
        SignatureContent content = envelope.getSignatureContents().get(0);
        ResultHolder holder = new ResultHolder();
        new AnnotationsManifestExistenceRule(defaultRuleStateProvider).verify(holder, content);
        new AnnotationsManifestIntegrityRule(defaultRuleStateProvider).verify(holder, content);
        new SingleAnnotationManifestExistenceRule(defaultRuleStateProvider).verify(holder, content);
        new SingleAnnotationManifestIntegrityRule(defaultRuleStateProvider).verify(holder, content);
        new AnnotationDataExistenceRule(defaultRuleStateProvider).verify(holder, content);
        new AnnotationDataIntegrityRule(defaultRuleStateProvider).verify(holder, content);

        envelope.close();
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
    public void testFullyRemovableAnnotationPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_FULLY_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationFullyRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationDataRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_FULLY_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testFullyRemovableAnnotationDataCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_FULLY_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_VALUE_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataRemoved_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationDataCorrupt_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_VALUE_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testValueRemovableAnnotationCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_VALUE_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationDataPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationDataRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION_DATA);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationDataCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION_DATA);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_MISSING_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testNonRemovableAnnotationCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_CORRUPT_ANNOTATION);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testAnnotationsManifestPresent_OK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_NON_REMOVABLE_ANNOTATION);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testAnnotationsManifestRemoved_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_MISSING_ANNOTATIONS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testAnnotationsManifestCorrupt_NOK() throws Exception {
        RuleVerificationResult result = getRuleVerificationResults(CONTAINER_WITH_CORRUPT_ANNOTATIONS_MANIFEST);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }
}
