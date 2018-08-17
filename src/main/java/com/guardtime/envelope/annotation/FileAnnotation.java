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

package com.guardtime.envelope.annotation;

import com.guardtime.envelope.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Annotation that is based on {@link File} as the data source.
 */
class FileAnnotation extends AbstractAnnotation {

    private final File file;

    /**
     * Creates annotation of given type, specific file as a data source.
     *
     * @param file the {@link File} containing the annotation value.
     * @param domain the annotation key.
     * @param type annotation type, indicating the persistence of the annotation, see {@link EnvelopeAnnotationType} for details.
     */

    FileAnnotation(File file, String domain, EnvelopeAnnotationType type) {
        super(domain, type);
        Util.notNull(file, "File");
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

}
