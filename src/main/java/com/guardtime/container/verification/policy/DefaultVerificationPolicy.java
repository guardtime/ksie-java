package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link VerificationPolicy}
 * Contains rules for:
 * <ol>
 *   <li>verifying manifest indexes are consecutive</li>
 *   <li>verifying {@link com.guardtime.container.manifest.DataFilesManifest}</li>
 *   <li>verifying {@link com.guardtime.container.datafile.ContainerDocument}s</li>
 *   <li>verifying {@link com.guardtime.container.manifest.AnnotationsManifest}</li>
 *   <li>verifying {@link com.guardtime.container.manifest.AnnotationInfoManifest}s</li>
 *   <li>verifying {@link com.guardtime.container.annotation.ContainerAnnotation}s</li>
 * </ol>
 * May contain extra rules to add specialized verification requirements to the policy or to overwrite some of the
 * pre-existing rules.
 */
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
