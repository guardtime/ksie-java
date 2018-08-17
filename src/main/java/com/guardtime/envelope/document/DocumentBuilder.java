package com.guardtime.envelope.document;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStore;
import com.guardtime.ksi.hashing.DataHash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.guardtime.envelope.util.Util.notEmpty;
import static com.guardtime.envelope.util.Util.notNull;

public class DocumentBuilder {
  private File fileContent;
  private String documentName;
  private String documentMimeType;
  private List<DataHash> hashContent;
  private EnvelopeElement elementContent;
  private ParsingStoreReference contentReference;

  public DocumentBuilder withDocumentName(String name) {
    notNull(name, "File name");
    this.documentName = name;
    return this;
  }

  public DocumentBuilder withDocumentMimeType(String mimeType) {
    notNull(mimeType, "MIME type");
    this.documentMimeType = mimeType;
    return this;
  }

  public DocumentBuilder withContent(File file) {
    notNull(file, "File");
    this.fileContent = file;
    return this;
  }

  public DocumentBuilder withDataHashList(Collection<DataHash> hashList) {
    notEmpty(hashList, "Data hash list");
    this.hashContent = new ArrayList<>(hashList);
    return this;
  }

  public DocumentBuilder withContent(EnvelopeElement element) {
    notNull(element, "EnvelopeElement");
    this.elementContent = element;
    return this;
  }

  public DocumentBuilder withParsingStoreReference(ParsingStoreReference reference) {
    notNull(reference, "Parsing store reference");
    this.contentReference = reference;
    return this;
  }

  public DocumentBuilder withContent(InputStream stream) {
    notNull(stream, "Input stream");
    this.contentReference = addToStore(stream, "StoredDocument-" + documentName + "-" + documentMimeType);
    return this;
  }

  public DocumentBuilder withDocument(Document original) {
    this.documentName = original.getFileName();
    this.documentMimeType = original.getMimeType();
    // TODO: Any better option than case with hardcoded strings?
    switch (original.getClass().getCanonicalName()) {
      case "FileDocument":
        return withContent(((FileDocument) original).file);
      case "EmptyDocument":
        return withDataHashList(((EmptyDocument) original).dataHashMap.values());
      case "InternalDocument":
        return withContent(((InternalDocument) original).element);
      default:
        try (InputStream inputStream = original.getInputStream()) {
          return this.withContent(inputStream);
        } catch (IOException e) {
          throw new IllegalArgumentException("Failed to access content of Document!", e);
        }
    }
  }

  public Document build() {
    if (fileContent != null) {
      return new FileDocument(fileContent, documentMimeType, documentName);
    }
    if (hashContent != null) {
      return new EmptyDocument(documentName, documentMimeType, hashContent);
    }
    if (elementContent != null) {
      return new InternalDocument(elementContent);
    }
    if (contentReference != null) {
      return new ParsedDocument(contentReference, documentMimeType, documentName);
    }
    throw new IllegalStateException("Document content not provided!");
  }

  private static ParsingStoreReference addToStore(InputStream data, String name) {
    notNull(data, "Input stream");
    try {
      return TemporaryFileBasedParsingStore.getInstance().store(name, data);
    } catch (ParsingStoreException e) {
      throw new IllegalArgumentException("Can not copy input stream to memory!", e);
    }
  }

}
