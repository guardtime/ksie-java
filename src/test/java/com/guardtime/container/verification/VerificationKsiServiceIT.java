package com.guardtime.container.verification;

import com.guardtime.container.AbstractCommonKsiServiceIntegrationTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.context.SimpleVerificationContext;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.ksi.KsiPolicyBasedSignatureIntegrityRule;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class VerificationKsiServiceIT extends AbstractCommonKsiServiceIntegrationTest {

    @Test
    public void testContainerWithInvalidSignature_VerificationFails() throws Exception {
        FileInputStream fis = new FileInputStream(loadFile(CONTAINER_WITH_WRONG_SIGNATURE_FILE_KSIE));
        Container container = packagingFactory.read(fis);
        VerificationPolicy defaultPolicy = new DefaultVerificationPolicy(Arrays.<Rule>asList(
                new KsiPolicyBasedSignatureIntegrityRule(ksi, new CalendarBasedVerificationPolicy()),
                new MimeTypeIntegrityRule(packagingFactory)
        ));
        ContainerVerifier verifier = new ContainerVerifier(defaultPolicy);
        VerificationContext context = new SimpleVerificationContext(container);
        VerifierResult verifierResult = verifier.verify(context);
        assertEquals(RuleResult.NOK, verifierResult.getVerificationResult());
    }
}
