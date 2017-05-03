package com.guardtime.container.verification;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.result.VerificationResultFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifiedSignatureContent extends SignatureContent {
    private final List<RuleVerificationResult> results;
    private final VerificationResult aggregateResult;
    private List<SignatureResult> signatureResults;


    VerifiedSignatureContent(SignatureContent original, ResultHolder holder) {
        super(
                new Builder()
                        .withManifest(original.getManifest())
                        .withSignature(original.getContainerSignature())
                        .withDocumentsManifest(original.getDocumentsManifest())
                        .withAnnotationsManifest(original.getAnnotationsManifest())
                        .withSingleAnnotationManifests(getSingleAnnotationManifests(original))
                        .withAnnotations(getAnnotations(original))
                        .withDocuments(original.getDocuments().values())
        );
        this.results = holder.getResults(original);
        this.signatureResults = holder.getSignatureResults(original);
        this.aggregateResult = holder.getFilteredAggregatedResult(new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results != null && results.contains(result);
            }
        });
    }

    private static List<Pair<String, ContainerAnnotation>> getAnnotations(SignatureContent original) {
        List<Pair<String, ContainerAnnotation>> annotationPairs = new ArrayList<>(original.getAnnotations().size());
        for (Map.Entry<String, ContainerAnnotation> entry : original.getAnnotations().entrySet()) {
            annotationPairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return annotationPairs;
    }

    private static List<Pair<String, SingleAnnotationManifest>> getSingleAnnotationManifests(SignatureContent original) {
        List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new ArrayList<>(original.getSingleAnnotationManifests().size());
        for (Map.Entry<String, SingleAnnotationManifest> entry : original.getSingleAnnotationManifests().entrySet()) {
            singleAnnotationManifestPairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return singleAnnotationManifestPairs;
    }

    /**
     * Provides access to {@link SignatureResult}s related to this {@link SignatureContent}.
     */
    public List<SignatureResult> getSignatureResults() {
        return signatureResults;
    }

    /**
     * Provides access to {@link RuleVerificationResult}s related to this {@link SignatureContent}.
     */
    public List<RuleVerificationResult> getResults() {
        return results;
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

}
