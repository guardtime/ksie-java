package com.guardtime.container.verification;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifiedSignatureContent extends SignatureContent {
    private final List<RuleVerificationResult> results;
    private final VerificationResult aggregateResult;


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
        this.results = extractResultsForSignatureContent(holder, original);
        this.aggregateResult = findHighestPriorityResult(results);
    }

    private List<RuleVerificationResult> extractResultsForSignatureContent(ResultHolder holder, SignatureContent original) {
        // TODO: filtering
        return holder.getResults();
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


    // TODO: Move to somewhere else as a public helper method so it won't have to be repeated
    private VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        for (RuleVerificationResult result : verificationResults) {
            VerificationResult verificationResult = result.getVerificationResult();
            if (verificationResult.isMoreImportantThan(returnable)) {
                returnable = verificationResult;
                if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }

}
