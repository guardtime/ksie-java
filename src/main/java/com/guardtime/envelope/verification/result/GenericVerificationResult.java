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

public class GenericVerificationResult implements RuleVerificationResult {
    private final VerificationResult result;
    private final String ruleName;
    private final String testedElement;
    private String ruleMessage;

    public GenericVerificationResult(VerificationResult result, String ruleName, String ruleMessage, String testedElement,
                                     Exception exception) {
        this(result, ruleName, ruleMessage, testedElement);
        this.ruleMessage = exception.getMessage();
    }


    public GenericVerificationResult(VerificationResult result, String ruleName, String ruleMessage, String testedElement) {
        this.result = result;
        this.testedElement = testedElement;
        this.ruleName = ruleName;
        this.ruleMessage = ruleMessage;
    }

    @Override
    public VerificationResult getVerificationResult() {
        return result;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String getRuleErrorMessage() {
        return ruleMessage;
    }


    @Override
    public String getTestedElementPath() {
        return testedElement;
    }

}
