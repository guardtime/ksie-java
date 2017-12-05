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

package com.guardtime.envelope.packaging;

import com.guardtime.envelope.indexing.IndexProvider;

/**
 * Helper class for generating manifest, annotation data and signature URI strings.
 */
public class EntryNameProvider {

    public static final String META_INF = "META-INF";
    public static final String DOCUMENTS_MANIFEST_FORMAT = META_INF + "/" + "datamanifest-%s.%s";
    public static final String MANIFEST_FORMAT = META_INF + "/" + "manifest-%s.%s";
    public static final String ANNOTATIONS_MANIFEST_FORMAT = META_INF + "/" + "annotmanifest-%s.%s";
    public static final String SIGNATURE_FORMAT = META_INF + "/" + "signature-%s.%s";
    public static final String SINGLE_ANNOTATION_MANIFEST_FORMAT = META_INF + "/" + "annotation-%s.%s";
    public static final String ANNOTATION_DATA_FORMAT = META_INF + "/" + "annotation-%s.dat";
    private final String manifestSuffix;
    private final String signatureSuffix;
    private final IndexProvider indexProvider;

    public EntryNameProvider(String manifestSuffix, String signatureSuffix, IndexProvider indexProvider) {
        this.manifestSuffix = manifestSuffix;
        this.signatureSuffix = signatureSuffix;
        this.indexProvider = indexProvider;
    }

    public String nextDocumentsManifestName() {
        return String.format(DOCUMENTS_MANIFEST_FORMAT, indexProvider.getNextDocumentsManifestIndex(), manifestSuffix);
    }

    public String nextManifestName() {
        return String.format(MANIFEST_FORMAT, indexProvider.getNextManifestIndex(), manifestSuffix);
    }

    public String nextAnnotationsManifestName() {
        return String.format(ANNOTATIONS_MANIFEST_FORMAT, indexProvider.getNextAnnotationsManifestIndex(), manifestSuffix);
    }

    public String nextSignatureName() {
        return String.format(SIGNATURE_FORMAT, indexProvider.getNextSignatureIndex(), signatureSuffix);
    }

    public String nextSingleAnnotationManifestName() {
        return String.format(SINGLE_ANNOTATION_MANIFEST_FORMAT, indexProvider.getNextSingleAnnotationManifestIndex(), manifestSuffix);
    }

    public String nextAnnotationDataFileName() {
        return String.format(ANNOTATION_DATA_FORMAT, indexProvider.getNextAnnotationIndex());
    }
}
