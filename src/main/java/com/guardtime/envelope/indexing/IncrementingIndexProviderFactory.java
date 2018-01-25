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

package com.guardtime.envelope.indexing;

import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.guardtime.envelope.packaging.EntryNameProvider.ANNOTATIONS_MANIFEST_FORMAT;
import static com.guardtime.envelope.packaging.EntryNameProvider.ANNOTATION_DATA_FORMAT;
import static com.guardtime.envelope.packaging.EntryNameProvider.DOCUMENTS_MANIFEST_FORMAT;
import static com.guardtime.envelope.packaging.EntryNameProvider.MANIFEST_FORMAT;
import static com.guardtime.envelope.packaging.EntryNameProvider.SIGNATURE_FORMAT;
import static com.guardtime.envelope.packaging.EntryNameProvider.SINGLE_ANNOTATION_MANIFEST_FORMAT;

/**
 * Produces {@link IndexProvider} that provides integer values that increment for each index. Continues from last used index of
 * provided {@link Envelope}
 */
public class IncrementingIndexProviderFactory implements IndexProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(IncrementingIndexProviderFactory.class);
    private final Pattern manifestMatcher = Pattern.compile(String.format(MANIFEST_FORMAT, "[0-9]+", "*"));
    private final Pattern annotationsManifestMatcher = Pattern.compile(String.format(ANNOTATIONS_MANIFEST_FORMAT, "[0-9]+", "*"));
    private final Pattern documentsManifestMatcher = Pattern.compile(String.format(DOCUMENTS_MANIFEST_FORMAT, "[0-9]+", "*"));
    private final Pattern signatureMatcher = Pattern.compile(String.format(SIGNATURE_FORMAT, "[0-9]+", "*"));
    private final Pattern singleAnnotationManifestMatcher =
            Pattern.compile(String.format(SINGLE_ANNOTATION_MANIFEST_FORMAT, "[0-9]+", "*"));
    private final Pattern annotationMatcher = Pattern.compile(String.format(ANNOTATION_DATA_FORMAT, "[0-9]+", "*"));

    @Override
    public IndexProvider create() {
        return new IncrementingIndexProvider();
    }

    @Override
    public IndexProvider create(Envelope envelope) {
        Set<String> manifestUriSet = new HashSet<>();
        Set<String> annotationUriSet = new HashSet<>();
        for (SignatureContent content : envelope.getSignatureContents()) {
            manifestUriSet.add(content.getManifest().getPath());
            manifestUriSet.add(content.getDocumentsManifest().getPath());
            manifestUriSet.add(content.getAnnotationsManifest().getPath());

            Manifest manifest = content.getManifest();
            if (manifest != null && manifest.getSignatureReference() != null) {
                manifestUriSet.add(manifest.getSignatureReference().getUri());
            }
            annotationUriSet.addAll(content.getSingleAnnotationManifests().keySet());
            annotationUriSet.addAll(content.getAnnotations().keySet());
        }
        for (UnknownDocument doc : envelope.getUnknownFiles()) {
            String fileName = doc.getFileName();
            if (manifestMatcher.matcher(fileName).matches() || documentsManifestMatcher.matcher(fileName).matches() ||
                    annotationsManifestMatcher.matcher(fileName).matches() || signatureMatcher.matcher(fileName).matches()) {
                manifestUriSet.add(fileName);
            }
            if (annotationMatcher.matcher(fileName).matches() || singleAnnotationManifestMatcher.matcher(fileName).matches()) {
                annotationUriSet.add(fileName);
            }
        }
        int maxIndex = findMaxFromSet(manifestUriSet);
        int maxAnnotationIndex = findMaxFromSet(annotationUriSet);

        return new IncrementingIndexProvider(maxIndex, maxIndex, maxIndex, maxIndex, maxAnnotationIndex, maxAnnotationIndex);
    }

    private int findMaxFromSet(Set<String> set) {
        int value = 0;
        for (String str : set) {
            value = Math.max(getIndex(str), value);
        }
        return value;
    }

    private int getIndex(String str)  {
        str = str.substring(str.lastIndexOf("/") + 1);
        String index = str.substring(str.indexOf("-") + 1, str.lastIndexOf("."));
        if (!index.equals(index.replaceAll("[^0-9]", ""))) {
            logger.warn("Not an integer based index");
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(index);
    }

    private class IncrementingIndexProvider implements IndexProvider {
        private int documentsManifestIndex = 0;
        private int manifestIndex = 0;
        private int signatureIndex = 0;
        private int annotationsManifestIndex = 0;
        private int singleAnnotationManifestIndex = 0;
        private int annotationIndex = 0;

        IncrementingIndexProvider() {
        }

        IncrementingIndexProvider(int documentsManifestIndex, int manifestIndex, int signatureIndex, int annotationsManifestIndex,
                                  int singleAnnotationManifestIndex, int annotationIndex) {
            this.documentsManifestIndex = documentsManifestIndex;
            this.manifestIndex = manifestIndex;
            this.signatureIndex = signatureIndex;
            this.annotationsManifestIndex = annotationsManifestIndex;
            this.singleAnnotationManifestIndex = singleAnnotationManifestIndex;
            this.annotationIndex = annotationIndex;
        }

        @Override
        public String getNextDocumentsManifestIndex() {
            return Integer.toString(++documentsManifestIndex);
        }

        @Override
        public String getNextManifestIndex() {
            return Integer.toString(++manifestIndex);
        }

        @Override
        public String getNextAnnotationsManifestIndex() {
            return Integer.toString(++annotationsManifestIndex);
        }

        @Override
        public String getNextSignatureIndex() {
            return Integer.toString(++signatureIndex);
        }

        @Override
        public String getNextSingleAnnotationManifestIndex() {
            return Integer.toString(++singleAnnotationManifestIndex);
        }

        @Override
        public String getNextAnnotationIndex() {
            return Integer.toString(++annotationIndex);
        }

    }

}
