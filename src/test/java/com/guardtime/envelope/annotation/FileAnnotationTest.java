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

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileAnnotationTest extends AbstractEnvelopeTest {

    @Test
    public void testCreateFileAnnotationWithoutInputFile_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File must be present");
        new FileAnnotation(null, ANNOTATION_DOMAIN_COM_GUARDTIME, EnvelopeAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutDomain_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Domain must be present");
        new FileAnnotation(new File(TEST_FILE_PATH_TEST_TXT), null, EnvelopeAnnotationType.NON_REMOVABLE);
    }

    @Test
    public void testCreateFileAnnotationWithoutAnnotationType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Annotation type must be present");
        new FileAnnotation(new File(TEST_FILE_PATH_TEST_TXT), ANNOTATION_DOMAIN_COM_GUARDTIME, null);
    }

    @Test
    public void testCreateNewFileAnnotation() throws Exception {
        FileAnnotation annotation = new FileAnnotation(
                loadFile(TEST_FILE_PATH_TEST_TXT),
                ANNOTATION_DOMAIN_COM_GUARDTIME,
                EnvelopeAnnotationType.NON_REMOVABLE
        );
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, annotation.getDomain());
        assertEquals(EnvelopeAnnotationType.NON_REMOVABLE, annotation.getAnnotationType());
        assertNotNull(annotation.getDataHash(HashAlgorithm.SHA2_256));
    }


    @Test
    public void testCloseDoesNotDeleteFile() throws Exception {
        File file = loadFile(TEST_FILE_PATH_TEST_TXT);
        FileAnnotation annotation = new FileAnnotation(
                file,
                ANNOTATION_DOMAIN_COM_GUARDTIME,
                EnvelopeAnnotationType.NON_REMOVABLE
        );
        annotation.close();
        assertTrue(file.exists());
    }

}