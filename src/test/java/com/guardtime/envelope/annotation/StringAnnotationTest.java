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

package com.guardtime.envelope.annotation;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringAnnotationTest extends AbstractEnvelopeTest {


    @Test
    public void testCreateStringAnnotationWithoutInputString_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Content must be present");
        new StringAnnotation(EnvelopeAnnotationType.NON_REMOVABLE, null, ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateStringAnnotationWithoutAnnotationType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation type must be present");
        new StringAnnotation(null, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        StringAnnotation annotation = new StringAnnotation(EnvelopeAnnotationType.NON_REMOVABLE, "Example Content", ANNOTATION_DOMAIN_COM_GUARDTIME);
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotation.getDomain());
        assertEquals(EnvelopeAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }


}