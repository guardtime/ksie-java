package com.guardtime.container.extending.ksi;

import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

/**
 * Extends all {@link KSISignature}s in {@link com.guardtime.container.packaging.Container} to specified publication
 * record. The publication time of the publication record must be after signature aggregation time.
 */
public class PublicationKsiContainerSignatureExtendingPolicy implements ExtendingPolicy<KSISignature> {
    private final PublicationRecord publicationRecord;
    protected final KSI ksi;

    public PublicationKsiContainerSignatureExtendingPolicy(KSI ksi, PublicationRecord publicationRecord) {
        Util.notNull(ksi, "KSI");
        Util.notNull(publicationRecord, "Publication record");
        this.ksi = ksi;
        this.publicationRecord = publicationRecord;
    }

    public KSISignature getExtendedSignature(KSISignature ksiSignature) throws SignatureException {
        try {
            return ksi.extend(ksiSignature, publicationRecord);
        } catch (KSIException e) {
            throw new SignatureException(e);
        }
    }

}
