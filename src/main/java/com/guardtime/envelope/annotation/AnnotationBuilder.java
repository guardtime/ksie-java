package com.guardtime.envelope.annotation;

import com.guardtime.envelope.packaging.parsing.store.MemoryBasedParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Universal builder for {@link Annotation}
 */
public class AnnotationBuilder {
  private String domain;
  private File fileContent;
  private String stringContent;
  private ParsingStoreReference contentReference;
  private EnvelopeAnnotationType type;

  public AnnotationBuilder withDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public AnnotationBuilder withAnnotationType(EnvelopeAnnotationType annotationType) {
    this.type = annotationType;
    return this;
  }

  public AnnotationBuilder withContent(File file) {
    notNull(file, "Content");
    this.fileContent = file;
    return this;
  }

  public AnnotationBuilder withContent(String content) {
    notNull(content, "Content");
    this.stringContent = content;
    return this;
  }

  public AnnotationBuilder withParsingStoreReference(ParsingStoreReference reference) {
    notNull(reference, "Parsing store reference");
    this.contentReference = reference;
    return this;
  }

  /**
   * NB! Does not close the stream! Just reads from it.
   */
  public AnnotationBuilder withContent(InputStream stream) {
    notNull(stream, "Content");
    this.contentReference = addToStore(stream, "StoredAnnotation-" + domain + "-" + type.getContent());
    return this;
  }

  public AnnotationBuilder withAnnotation(Annotation annotation) {
    this.domain = annotation.getDomain();
    this.type = annotation.getAnnotationType();
    try (InputStream inputStream = annotation.getInputStream()) {
      return this.withContent(inputStream);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to access content of provided Annotation.", e);
    }
  }

  public Annotation build() {
    if (fileContent != null) {
      return new FileAnnotation(fileContent, domain, type);
    }
    if (stringContent != null) {
      return new StringAnnotation(stringContent, domain, type);
    }
    if (contentReference != null) {
      return new ParsedAnnotation(contentReference, domain, type);
    }
    throw new IllegalStateException("Annotation content not provided!");
  }

  private static ParsingStoreReference addToStore(InputStream data, String name) {
    notNull(data, "Input stream");
    try {
      return MemoryBasedParsingStore.getInstance().store(name, data);
    } catch (ParsingStoreException e) {
      throw new IllegalArgumentException("Can not copy input stream to memory!", e);
    }
  }
}
