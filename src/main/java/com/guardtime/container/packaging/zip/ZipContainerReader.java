package com.guardtime.container.packaging.zip;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.zip.handler.*;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for reading Zip container.
 */
class ZipContainerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipContainerReader.class);

    private final DataFileContentHandler documentHandler = new DataFileContentHandler();
    private final AnnotationContentHandler annotationContentHandler = new AnnotationContentHandler();
    private final UnknownFileHandler unknownFileHandler = new UnknownFileHandler();
    private final MimeTypeHandler mimeTypeHandler = new MimeTypeHandler();
    private final ManifestHolder manifestHandler;
    private final DataManifestHandler dataManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final AnnotationManifestHandler annotationManifestHandler;
    private final SignatureHandler signatureHandler;
    private final SignatureContentHandler signatureContentHandler;

    private ContentHandler[] handlers;

    ZipContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory) {
        this.manifestHandler = new ManifestHolder(manifestFactory);
        this.dataManifestHandler = new DataManifestHandler(manifestFactory);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory);
        this.annotationManifestHandler = new AnnotationManifestHandler(manifestFactory);
        this.signatureHandler = new SignatureHandler(signatureFactory);
        this.handlers = new ContentHandler[]{mimeTypeHandler, documentHandler, annotationContentHandler, dataManifestHandler,
                manifestHandler, annotationsManifestHandler, signatureHandler, annotationManifestHandler};

        this.signatureContentHandler = new SignatureContentHandler(documentHandler, annotationContentHandler, manifestHandler,
                dataManifestHandler, annotationsManifestHandler, annotationManifestHandler, signatureHandler);
    }

    ZipBlockChainContainer read(InputStream input) throws IOException {
        try (ZipInputStream zipInput = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    LOGGER.trace("Skipping directory '{}'", entry.getName());
                    continue;
                }
                readEntry(zipInput, entry);
            }
        }
        List<ZipSignatureContent> contents = buildSignatures();
        MimeType mimeType = getMimeType();
        List<Pair<String, File>> unknownFiles = getUnknownFiles();
        return new ZipBlockChainContainer(contents, unknownFiles, mimeType);
    }

    private MimeType getMimeType() {
        try {
            String uri = ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME;
            byte[] content = mimeTypeHandler.get(uri);
            return new MimeTypeEntry(uri, content);
        } catch (FileParsingException e) {
            return null;
        }
    }

    private List<Pair<String, File>> getUnknownFiles() {
        List<Pair<String, File>> returnable = new LinkedList<>();
        returnable.addAll(unknownFileHandler.getUnrequestedFiles());
        for (ContentHandler handler : handlers) {
            returnable.addAll(handler.getUnrequestedFiles());
        }
        return returnable;
    }

    private void readEntry(ZipInputStream zipInput, ZipEntry entry) throws IOException {
        String name = entry.getName();
        File tempFile = createTempFile();
        com.guardtime.ksi.util.Util.copyData(zipInput, new FileOutputStream(tempFile));
        for (ContentHandler handler : handlers) {
            if (handler.isSupported(name)) {
                LOGGER.info("Reading zip entry '{}'. Using handler '{}' ", name, handler.getClass().getName());
                handler.add(name, tempFile);
                return;
            }
        }
        unknownFileHandler.add(name, tempFile);
    }

    private List<ZipSignatureContent> buildSignatures() {
        Set<String> signatureManifests = manifestHandler.getNames();
        List<ZipSignatureContent> signatures = new LinkedList<>();
        for (String manifest : signatureManifests) {
            try {
                signatures.add(signatureContentHandler.get(manifest));
            } catch (BlockChainContainerException e) {
                LOGGER.info("Failed to parse SignatureContent for manifest '{}'", manifest);
            }
        }
        return signatures;
    }

    private File createTempFile() throws IOException {
        return Util.createTempFile("ksie_", ".tmp");
    }

}