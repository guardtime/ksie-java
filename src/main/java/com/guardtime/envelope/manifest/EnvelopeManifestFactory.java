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

package com.guardtime.envelope.manifest;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.util.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Creates or parses manifests used for envelope internal structure.
 * @param <M>     Signature manifest implementation.
 * @param <D>     Data files manifest implementation.
 * @param <A>     Annotations manifest implementation.
 * @param <SA>    Annotation info manifest implementation.
 */
public interface EnvelopeManifestFactory<M extends Manifest, D extends DocumentsManifest, A extends AnnotationsManifest, SA extends SingleAnnotationManifest> {

    HashAlgorithmProvider getHashAlgorithmProvider();

    M createManifest(Pair<String, D> documentsManifest, Pair<String, A> annotationManifest, Pair<String, String> signatureReference) throws InvalidManifestException;

    D createDocumentsManifest(List<Document> files) throws InvalidManifestException;

    A createAnnotationsManifest(Map<String, Pair<Annotation, SA>> annotationManifests) throws InvalidManifestException;

    SA createSingleAnnotationManifest(Pair<String, D> documentsManifest, Pair<String, Annotation> annotation) throws InvalidManifestException;

    M readManifest(InputStream input) throws InvalidManifestException;

    D readDocumentsManifest(InputStream input) throws InvalidManifestException;

    A readAnnotationsManifest(InputStream input) throws InvalidManifestException;

    SA readSingleAnnotationManifest(InputStream input) throws InvalidManifestException;

    ManifestFactoryType getManifestFactoryType();

}
