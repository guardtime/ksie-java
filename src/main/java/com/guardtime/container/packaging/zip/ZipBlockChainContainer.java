package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.BCCMimeType;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.ksi.util.Util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockChainContainer implements BlockChainContainer {

    private List<SignatureContent> signatureContents = new LinkedList<>();
    private BCCMimeType mimeType;

    public ZipBlockChainContainer(SignatureContent signatureContent) {
        this.signatureContents.add(signatureContent);
        this.mimeType = new MimeTypeEntry();
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents, BCCMimeType mimeType) {
        this.signatureContents = signatureContents;
        this.mimeType = mimeType;
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents) {
        this(signatureContents, new MimeTypeEntry());
    }

    @Override
    public List<SignatureContent> getSignatureContents() {
        return signatureContents;
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeMimeTypeEntry(zipOutputStream);
            writeSignatures(signatureContents, zipOutputStream);
        }
    }

    private void writeMimeTypeEntry(ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry mimeTypeEntry = new ZipEntry(mimeType.getUri());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Util.copyData(mimeType.getInputStream(), bos);
        byte[] data = bos.toByteArray();
        mimeTypeEntry.setSize(data.length);
        mimeTypeEntry.setCompressedSize(data.length);
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        mimeTypeEntry.setCrc(checksum.getValue());
        mimeTypeEntry.setMethod(ZipEntry.STORED);
        writeEntry(mimeTypeEntry, mimeType.getInputStream(), zipOutputStream);
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
