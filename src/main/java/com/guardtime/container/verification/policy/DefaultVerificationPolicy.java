package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultVerificationPolicy implements VerificationPolicy {
    private Map<String, Rule> rules = new HashMap<>();

    public DefaultVerificationPolicy(List<Rule> extraRules) {
        addDefaultRules();
        addAdditionalAndReplaceMatchingRules(extraRules);
    }

    private void addAdditionalAndReplaceMatchingRules(List<Rule> extraRules) {
        for (Rule rule : extraRules) {
            rules.put(rule.getName(), rule);
        }
    }

    private void addDefaultRules() {
        ManifestConsecutivityRule manifestConsecutivityRule = new ManifestConsecutivityRule();
        rules.put(manifestConsecutivityRule.getName(), manifestConsecutivityRule);
        DataFilesManifestIntegrityRule dataFilesManifestIntegrityRule = new DataFilesManifestIntegrityRule();
        rules.put(dataFilesManifestIntegrityRule.getName(), dataFilesManifestIntegrityRule);
        DataFileIntegrityRule dataFileIntegrityRule = new DataFileIntegrityRule();
        rules.put(dataFileIntegrityRule.getName(), dataFileIntegrityRule);
        AnnotationsManifestIntegrityRule annotationsManifestIntegrityRule = new AnnotationsManifestIntegrityRule();
        rules.put(annotationsManifestIntegrityRule.getName(), annotationsManifestIntegrityRule);
        AnnotationInfoManifestIntegrityRule annotationInfoManifestIntegrityRule = new AnnotationInfoManifestIntegrityRule();
        rules.put(annotationInfoManifestIntegrityRule.getName(), annotationInfoManifestIntegrityRule);
        AnnotationDataIntegrityRule annotationDataIntegrityRule = new AnnotationDataIntegrityRule();
        rules.put(annotationDataIntegrityRule.getName(), annotationDataIntegrityRule);
    }

    @Override
    public List<Rule> getRules() {
        return new LinkedList<>(rules.values());
    }

}
