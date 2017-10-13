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

package com.guardtime.envelope.verification.result;

/**
 * Results produced during verification which group together the rule {@link VerificationResult} and rule data like rule
 * name and error message and path of the tested envelope component.
 */
public interface RuleVerificationResult {

    VerificationResult getVerificationResult();

    /**
     * Indicates which rule was used to produce this result by referring to the rules unique name string.
     */
    String getRuleName();

    /**
     * Contains the message string provided by the rule which applies for a non OK result.
     */
    String getRuleErrorMessage();

    /**
     * Provides path of the element that the verification was performed on by the rule. This is a helper for
     * sorting/distinguishing between results for different elements contained in the envelope.
     */
    String getTestedElementPath();

}
