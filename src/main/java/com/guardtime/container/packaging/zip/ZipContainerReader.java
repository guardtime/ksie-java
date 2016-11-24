package com.guardtime.container.packaging.zip;

import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.zip.handler.AnnotationContentHandler;
import com.guardtime.container.packaging.zip.handler.AnnotationsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ContentHandler;
import com.guardtime.container.packaging.zip.handler.ContentParsingException;
import com.guardtime.container.packaging.zip.handler.DocumentContentHandler;
import com.guardtime.container.packaging.zip.handler.DocumentsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ManifestHandler;
import com.guardtime.container.packaging.zip.handler.MimeTypeHandler;
import com.guardtime.container.packaging.zip.handler.SignatureHandler;
import com.guardtime.container.packaging.zip.handler.SingleAnnotationManifestHandler;
import com.guardtime.container.packaging.zip.handler.UnknownFileHandler;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.guardtime.container.util.Util.createTempFile;
import static com.guardtime.container.util.Util.getTempDirectory;

/**
 * Helper class for reading Zip container.
 */
class ZipContainerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipContainerReader.class);

    private final DocumentContentHandler documentHandler = new DocumentContentHandler();
    private final AnnotationContentHandler annotationContentHandler = new AnnotationContentHandler();
    private final UnknownFileHandler unknownFileHandler = new UnknownFileHandler();
    private final MimeTypeHandler mimeTypeHandler = new MimeTypeHandler();
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;
    private final SignatureContentHandler signatureContentHandler;
    private final Path tempDirectory;

    private ContentHandler[] handlers;

    ZipContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory) throws IOException {
        this.tempDirectory = getTempDirectory();
        this.manifestHandler = new ManifestHandler(manifestFactory);
        this.documentsManifestHandler = new DocumentsManifestHandler(manifestFactory);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory);
        this.singleAnnotationManifestHandler = new SingleAnnotationManifestHandler(manifestFactory);
        this.signatureHandler = new SignatureHandler(signatureFactory);
        this.handlers = new ContentHandler[]{mimeTypeHandler, documentHandler, annotationContentHandler, documentsManifestHandler,
                manifestHandler, annotationsManifestHandler, signatureHandler, singleAnnotationManifestHandler};

        this.signatureContentHandler = new SignatureContentHandler(documentHandler, annotationContentHandler, manifestHandler,
                documentsManifestHandler, annotationsManifestHandler, singleAnnotationManifestHandler, signatureHandler);
    }

    ZipContainer read(InputStream input) throws IOException, InvalidPackageException {
        try(ZipInputStream zipInput = new ZipInputStream(input)) {
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
        List<UnknownDocument> unknownFiles = getUnknownFiles();

        if (validMimeType(mimeType) && containsValidContents(contents)) {
            return new ZipContainer(contents, unknownFiles, mimeType, tempDirectory);
        } else {
            throw new InvalidPackageException("Parsed container was not valid");
        }
    }

    private boolean containsValidContents(List<ZipSignatureContent> signatureContents) {
        for (SignatureContent content : signatureContents) {
            if (containsManifest(content) ||
                    containsOrContainedDocuments(content) ||
                    containsOrContainedAnnotations(content)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOrContainedDocuments(SignatureContent content) {
        return content.getDocuments().size() > 0 || content.getDocumentsManifest().getRight().getDocumentReferences().size() > 0;
    }

    private boolean containsOrContainedAnnotations(SignatureContent content) {
        return content.getAnnotations().size() > 0 || content.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().size() > 0;
    }

    private boolean containsManifest(SignatureContent content) {
        return content.getManifest() != null ||
                content.getDocumentsManifest() != null ||
                content.getAnnotationsManifest() != null;
    }

    private boolean validMimeType(MimeType mimeType) {
        if (mimeType == null) {
            return false;
        }
        try (InputStream inputStream = mimeType.getInputStream()) {
            return inputStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private MimeType getMimeType() {
        try {
            String uri = ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME;
            byte[] content = mimeTypeHandler.get(uri);
            return new MimeTypeEntry(uri, content);
        } catch (ContentParsingException e) {
            LOGGER.info("Failed to parse MIME type. Reason: '{}", e.getMessage());
            return null;
        }
    }

    private List<UnknownDocument> getUnknownFiles() throws IOException {
        List<UnknownDocument> returnable = new LinkedList<>();
        returnable.addAll(unknownFileHandler.getUnrequestedFiles());
        for (ContentHandler handler : handlers) {
            returnable.addAll(handler.getUnrequestedFiles());
        }
        return returnable;
    }

    private void readEntry(ZipInputStream zipInput, ZipEntry entry) throws IOException {
        String name = entry.getName();
        File tempFile = createTempFile(tempDirectory);
        Util.copyToTempFile(zipInput, tempFile);
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
        Set<String> parsedManifestUriSet = manifestHandler.getNames();
        List<ZipSignatureContent> signatures = new LinkedList<>();
        for (String manifestUri : parsedManifestUriSet) {
            try {
                signatures.add(signatureContentHandler.get(manifestUri));
            } catch (ContentParsingException e) {
                LOGGER.info("Parsing SignatureContent failed for '{}'. Reason: '{}'", manifestUri, e.getMessage());
            }
        }
        return signatures;
    }

}