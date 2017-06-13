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

import com.guardtime.container.packaging.SignatureContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultHolder {

    public static final VerificationResultFilter ALL = new VerificationResultFilter() {
        @Override
        public boolean apply(RuleVerificationResult result) {
            return true;
        }
    };
    public static final VerificationResultFilter NONE = new VerificationResultFilter() {
        @Override
        public boolean apply(RuleVerificationResult result) {
            return false;
        }
    };
    private final List<RuleVerificationResult> containerResults;
    private final Map<SignatureContent, List<RuleVerificationResult>> signatureContentResultsMap;
    private final Map<SignatureContent, List<SignatureResult>> signatureResultsMap = new HashMap<>();

    public ResultHolder() {
        this.containerResults = new ArrayList<>();
        this.signatureContentResultsMap = new HashMap<>();
    }

    /**
     * Returns all verification results gathered.
     */
    public List<RuleVerificationResult> getResults() {
        List<RuleVerificationResult> allResults = new ArrayList<>();
        allResults.addAll(containerResults);
        for (List<RuleVerificationResult> results : signatureContentResultsMap.values()) {
            allResults.addAll(results);
        }
        return allResults;
    }

    /**
     * Returns verification results specific to provided {@link SignatureContent} as well as generic results not specific to any
     * {@link SignatureContent}.
     * @param content for which verification results must apply.
     */
    public List<RuleVerificationResult> getResults(SignatureContent content) {
        List<RuleVerificationResult> contentVerificationResults = signatureContentResultsMap.get(content);
        if(contentVerificationResults == null) {
            return null;
        }
        List<RuleVerificationResult> ruleVerificationResults = new ArrayList<>(contentVerificationResults);
        ruleVerificationResults.addAll(containerResults); // Lets make sure that generic container rules are accessible as well
        return ruleVerificationResults;
    }

    /**
     * Returns root signature verification results specific to provided {@link SignatureContent}.
     * @param content for which root signature verification results must apply.
     */
    public List<SignatureResult> getSignatureResults(SignatureContent content) {
        return signatureResultsMap.get(content);
    }

    /**
     * Returns verification results that apply to a {@link com.guardtime.container.packaging.Container} and not to any one
     * specific {@link SignatureContent}.
     */
    public List<RuleVerificationResult> getGeneralResults() {
        return containerResults;
    }

    /**
     * Adds a {@link SignatureResult} to the result set for the provided {@link SignatureContent}.
     */
    public void addSignatureResult(SignatureContent content, SignatureResult result) {
        List<SignatureResult> signatureResults = signatureResultsMap.get(content);
        if (signatureResults == null) {
            signatureResults = new ArrayList<>();
            signatureResultsMap.put(content, signatureResults);
        }
        signatureResults.add(result);
    }

    /**
     * Adds the provided {@link RuleVerificationResult} to the general result set.
     */
    public void addResult(RuleVerificationResult ruleVerificationResult) {
        containerResults.add(ruleVerificationResult);
    }

    /**
     * Adds the provided list of {@link RuleVerificationResult}s to the general result set.
     */
    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        containerResults.addAll(ruleVerificationResults);
    }

    /**
     * Adds the provided {@link RuleVerificationResult} to the result set of the specified {@link SignatureContent}.
     */
    public void addResult(SignatureContent content, RuleVerificationResult ruleVerificationResult) {
        List<RuleVerificationResult> results = signatureContentResultsMap.get(content);
        if (results == null) {
            results = new ArrayList<>();
            signatureContentResultsMap.put(content, results);
        }
        results.add(ruleVerificationResult);
    }

    /**
     * Adds the provided list of {@link RuleVerificationResult}s to the result set of the specified {@link SignatureContent}.
     */
    public void addResults(SignatureContent content, List<RuleVerificationResult> ruleVerificationResults) {
        List<RuleVerificationResult> results = signatureContentResultsMap.get(content);
        if (results == null) {
            results = new ArrayList<>();
            signatureContentResultsMap.put(content, results);
        }
        results.addAll(ruleVerificationResults);
    }

    public VerificationResult getAggregatedResult() {
        return getFilteredAggregatedResult(ALL);
    }

    public VerificationResult getFilteredAggregatedResult(VerificationResultFilter filter) {
        return getFilteredAggregatedResult(filter, 0);
    }

    public VerificationResult getFilteredAggregatedResult(VerificationResultFilter filter, int resultsCount) {
        List<VerificationResult> filteredResults = new ArrayList<>();
        for (RuleVerificationResult result : getResults()) {
            if (filter.apply(result)) {
                filteredResults.add(result.getVerificationResult());
            }
        }
        if (filteredResults.size() < resultsCount) {
            return VerificationResult.NOK;
        }
        return findHighestPriorityResult(filteredResults);
    }

    /**
     * Finds the highest priority {@link VerificationResult} that exists in the given list of {@link VerificationResult}s.
     * The priorities of {@link VerificationResult} go from highest (NOK) to lowest (OK).
     */
    private VerificationResult findHighestPriorityResult(List<VerificationResult> verificationResults) {
        List<VerificationResult> sortedList = new ArrayList<>(verificationResults);
        sortedList.add(VerificationResult.OK); // So we always have at least one default value in list
        Collections.sort(sortedList, new Comparator<VerificationResult>() {
            @Override
            public int compare(VerificationResult verificationResult, VerificationResult t1) {
                return t1.compareTo(verificationResult);
            }
        });
        return sortedList.get(0);
    }
}
