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

package com.guardtime.container.verification.result;

/**
 * Possible results for any given rule used to verify a container.
 */
public enum VerificationResult {
    OK("RESULT_OK", 0),
    WARN("RESULT_WARN", 1),
    NOK("RESULT_NOK", 2);

    private final String name;
    private final int weight;

    VerificationResult(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    /**
     * Compares this with that to determine which hash higher priority.
     * @param that    The other {@link VerificationResult} to compare with
     * @return true when this has higher priority than that.
     */
    public boolean isMoreImportantThan(VerificationResult that) {
        return this.weight > that.weight;
    }
}
