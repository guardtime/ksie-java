package com.guardtime.container.verification.policy;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.*;

import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<Rule> rules = new ArrayList<>();

    public DefaultVerificationPolicy(List<Rule> extraRules) {
        addDefaultRules();
        addAdditionalAndReplaceMatchingRules(extraRules);
    }

    private void addAdditionalAndReplaceMatchingRules(List<Rule> extraRules) {
        for (Rule newRule : extraRules) {
            Integer index = getExistingRuleIndex(newRule);
            if(index != null) {
                rules.set(index, newRule);
            } else {
                rules.add(newRule);
            }
        }
    }

    private Integer getExistingRuleIndex(Rule newRule) {
        for (int i = 0; i < rules.size(); i++) {
            String ruleName = rules.get(i).getName();
            if (ruleName.equals(newRule.getName())) {
                return i;
            }
        }
        return null;
    }

    private void addDefaultRules() {
        rules.add(new ManifestConsecutivityRule());
        rules.add(new DataFilesManifestIntegrityRule());
        rules.add(new DataFileIntegrityRule());
        rules.add(new AnnotationsManifestIntegrityRule());
        rules.add(new AnnotationInfoManifestIntegrityRule());
        rules.add(new AnnotationDataIntegrityRule());
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

}
