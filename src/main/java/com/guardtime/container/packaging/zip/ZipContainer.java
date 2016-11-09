package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.guardtime.container.util.Util.deleteFileOrDirectory;

class ZipContainer implements Container {

    private final File temporaryDirectory;
    private List<ZipSignatureContent> signatureContents = new LinkedList<>();
    private MimeType mimeType;
    private List<Pair<String, File>> unknownFiles = new LinkedList<>();

    public ZipContainer(ZipSignatureContent signatureContent, MimeType mimeType) {
        this(Collections.singletonList(signatureContent), new LinkedList<Pair<String, File>>(), mimeType);
    }

    public ZipContainer(List<ZipSignatureContent> signatureContents, List<Pair<String, File>> unknownFiles, MimeType mimeType) {
        this(signatureContents, unknownFiles, mimeType, null);
    }

    public ZipContainer(List<ZipSignatureContent> contents, List<Pair<String, File>> unknownFiles, MimeType mimeType, File tempDirectory) {
        this.signatureContents.addAll(contents);
        this.unknownFiles.addAll(unknownFiles);
        this.mimeType = mimeType;
        this.temporaryDirectory = tempDirectory;
    }

    @Override
    public List<ZipSignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(signatureContents);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output));
        writeMimeTypeEntry(zipOutputStream);
        writeSignatures(signatureContents, zipOutputStream);
        writeExcessFiles(zipOutputStream);
        zipOutputStream.close();
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public List<Pair<String, File>> getUnknownFiles() {
        return Collections.unmodifiableList(unknownFiles);
    }

    @Override
    public void close() throws IOException {
        for (SignatureContent content : getSignatureContents()) {
            for (ContainerAnnotation annotation : content.getAnnotations().values()) {
                annotation.close();
            }

            for (ContainerDocument document : content.getDocuments().values()) {
                document.close();
            }
            for (Pair<String, File> f : getUnknownFiles()) {
                Files.deleteIfExists(f.getRight().toPath());
            }
        }
        deleteFileOrDirectory(temporaryDirectory);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
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

    private void writeSignatures(List<ZipSignatureContent> signatureContents, ZipOutputStream zipOutputStream) throws IOException {
        for (ZipSignatureContent signatureContent : signatureContents) {
            signatureContent.writeTo(zipOutputStream);
        }
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }
}
