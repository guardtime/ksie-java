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

package com.guardtime.envelope.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Document that is based on a {@link File}.
 */
class FileDocument extends AbstractDocument {

    protected final File file;

    /**
     * Creates {@link Document} with provided MIME-type and file.
     * @param mimeType The MIME-type of the {@link Document}.
     * @param file     The {@link File} to be used for the {@link Document} data and name.
     */
    FileDocument(File file, String mimeType) {
        this(file, mimeType, null);
    }

    /**
     * Creates {@link Document} with provided MIME-type and file and file name.
     * @param file     The {@link File} to be used for the {@link Document} data.
     * @param mimeType The MIME-type of the {@link Document}.
     * @param fileName The file name to be used for the {@link Document}.
     */
    FileDocument(File file, String mimeType, String fileName) {
        super(mimeType, getFileName(file, fileName));
        this.file = file;
    }

    private static String getFileName(File file, String fileName) {
        notNull(file, "File");
        return fileName == null ? file.getName() : fileName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

}
