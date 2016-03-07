package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.util.Pair;
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
    private MimeType mimeType;
    private List<Pair<String, File>> unknownFiles = new LinkedList<>();
    private ZipEntryNameProvider nameProvider;

    public ZipBlockChainContainer(SignatureContent signatureContent, MimeType mimeType, ZipEntryNameProvider nameProvider) {
        this.signatureContents.add(signatureContent);
        this.mimeType = mimeType;
        this.nameProvider = nameProvider;
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents, List<Pair<String, File>> unknownFiles, MimeType mimeType, ZipEntryNameProvider nameProvider) {
        this.signatureContents = signatureContents;
        this.unknownFiles = unknownFiles;
        this.mimeType = mimeType;
        this.nameProvider = nameProvider;
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
            writeExcessFiles(zipOutputStream);
        }
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    public ZipEntryNameProvider getNameProvider() {
        return nameProvider;
    }

    private void writeExcessFiles(ZipOutputStream zipOutputStream) throws IOException {
        for (Pair<String, File> file : unknownFiles) {
            writeEntry(new ZipEntry(file.getLeft()), new FileInputStream(file.getRight()), zipOutputStream);
        }
    }

    private void writeMimeTypeEntry(ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry mimeTypeEntry = new ZipEntry(mimeType.getUri());
        byte[] data = Util.toByteArray(mimeType.getInputStream());
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
