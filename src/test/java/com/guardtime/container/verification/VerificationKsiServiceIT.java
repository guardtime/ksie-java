package com.guardtime.container.verification;

import com.guardtime.container.AbstractCommonKsiServiceIntegrationTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.ksi.KsiPolicyBasedSignatureIntegrityRule;
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
        VerificationPolicy defaultPolicy = new DefaultVerificationPolicy(
                new KsiPolicyBasedSignatureIntegrityRule(ksi, new CalendarBasedVerificationPolicy()),
                new MimeTypeIntegrityRule(packagingFactory)
        );
        ContainerVerifier verifier = new ContainerVerifier(defaultPolicy);
        ContainerVerifierResult verifierResult = verifier.verify(container);
        assertEquals(VerificationResult.NOK, verifierResult.getVerificationResult());
    }
}
