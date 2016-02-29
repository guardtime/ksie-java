package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockChainContainer implements BlockChainContainer {

    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeTypeEntry mimeType;

    public ZipBlockChainContainer(SignatureContent signatureContent) {
        this.signatureContents.add(signatureContent);
        this.mimeType = new MimeTypeEntry();
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents) {
        this.signatureContents = signatureContents;
    }

    @Override
    public List<SignatureContent> getSignatureContents() {
        return signatureContents;
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeEntry(new ZipEntry(mimeType.getUri()), mimeType.getInputStream(), zipOutputStream);
            writeSignatures(signatureContents, zipOutputStream);
        }
    }

    private void writeSignatures(List<SignatureContent> signatureContents, ZipOutputStream zipOutputStream) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            signatureContent.writeTo(zipOutputStream);
        }
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }

}
