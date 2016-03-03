package com.guardtime.container.packaging.zip;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockChainContainer implements BlockChainContainer {

    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeTypeEntry mimeType;
    private List<Pair<String, File>> unknownFiles = new LinkedList<>();

    public ZipBlockChainContainer(SignatureContent signatureContent) {
        this.signatureContents.add(signatureContent);
        this.mimeType = new MimeTypeEntry();
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents, List<Pair<String, File>> unknownFiles) {
        this.signatureContents = signatureContents;
        this.unknownFiles = unknownFiles;
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
            writeExcessFiles(zipOutputStream);
        }
    }

    private void writeExcessFiles(ZipOutputStream zipOutputStream) throws IOException {
        for(Pair<String, File> file : unknownFiles) {
            writeEntry(new ZipEntry(file.getLeft()), new FileInputStream(file.getRight()), zipOutputStream);
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
