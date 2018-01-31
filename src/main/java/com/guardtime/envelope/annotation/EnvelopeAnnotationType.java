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

import java.util.HashMap;
import java.util.Map;

/**
 * Annotation types that define annotation persistence.
 */
public enum EnvelopeAnnotationType {

    /**
     * The whole annotation, meaning its value and the TLV element containing its meta-data, can be removed from the envelope
     * without breaking the integrity of the envelope's contents.
     */
    FULLY_REMOVABLE("ksie10/removable-fully"),

    /**
     * The value of the annotation can be removed without breaking the integrity of the envelope.
     * The TLV element containing the annotation's meta-data must stay intact.
     */
    VALUE_REMOVABLE("ksie10/removable-value"),

    /**
     * No part of the annotation can be removed without breaking the integrity of the envelope.
     */
    NON_REMOVABLE("ksie10/removable-none");

    private String content;
    private static Map<String, EnvelopeAnnotationType> types;

    static {
        types = new HashMap<>();
        types.put(FULLY_REMOVABLE.getContent(), FULLY_REMOVABLE);
        types.put(VALUE_REMOVABLE.getContent(), VALUE_REMOVABLE);
        types.put(NON_REMOVABLE.getContent(), NON_REMOVABLE);
    }

    EnvelopeAnnotationType(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public static EnvelopeAnnotationType fromContent(String content) {
        return types.get(content);
    }
}
