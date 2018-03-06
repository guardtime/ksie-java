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

package com.guardtime.envelope.verification.rule;

/**
 * Verification rule types. Verification policies can be different combinations of these rule types.
 */
public enum RuleType {
    /**
     * Does the data manifest exist?
     */
    KSIE_VERIFY_DATA_MANIFEST_EXISTS("KSIE_VERIFY_DATA_MANIFEST_EXISTS"),
    /**
     * Does the hash of the associated data manifest match the hash in the manifest file?
     */
    KSIE_VERIFY_DATA_MANIFEST("KSIE_VERIFY_DATA_MANIFEST"),
    /**
     * Does the data file exist?
     */
    KSIE_VERIFY_DATA_EXISTS("KSIE_VERIFY_DATA_EXISTS"),
    /**
     * ???
     */
    KSIE_VERIFY_DATA_HASH("KSIE_VERIFY_DATA_HASH"),
    /**
     * ???
     */
    KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS("KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS"),
    /**
     * Does the hash of the associated annotation match the hash in the manifest file?
     */
    KSIE_VERIFY_ANNOTATION_MANIFEST("KSIE_VERIFY_ANNOTATION_MANIFEST"),
    /**
     * Is the annotation meta-data mandatory (according to
     * {@link com.guardtime.envelope.annotation.EnvelopeAnnotationType})
     * and, if yes, does it exist?
     */
    KSIE_VERIFY_ANNOTATION_EXISTS("KSIE_VERIFY_ANNOTATION_EXISTS"),
    /**
     * Does the annotation's meta-data hash match the hash in the annotation's manifest?
     */
    KSIE_VERIFY_ANNOTATION("KSIE_VERIFY_ANNOTATION"),
    /**
     * Is the annotation data mandatory (according to
     * {@link com.guardtime.envelope.annotation.EnvelopeAnnotationType})
     * and, if yes, does it exist?
     */
    KSIE_VERIFY_ANNOTATION_DATA_EXISTS("KSIE_VERIFY_ANNOTATION_DATA_EXISTS"),
    /**
     * Does the annotation's data hash match the hash in the annotation's manifest?
     */
    KSIE_VERIFY_ANNOTATION_DATA("KSIE_VERIFY_ANNOTATION_DATA"),
    /**
     * Can the signature of the manifest be verified?
     */
    KSIE_VERIFY_SIGNATURE("KSIE_VERIFY_SIGNATURE"),
    /**
     * Does the signature exist?
     */
    KSIE_VERIFY_SIGNATURE_EXISTS("KSIE_VERIFY_SIGNATURE_EXISTS"),
    /**
     * Does the signature input hash match the manifest?
     */
    KSIE_VERIFY_MANIFEST("KSIE_VERIFY_MANIFEST");

    private final String name;

    RuleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
