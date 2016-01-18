package com.guardtime.container.annotation;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StringContainerAnnotation implements ContainerAnnotation {

    private String fileName;
    private String domain;
    private String content;

    public StringContainerAnnotation(String fileName, String domain, String content) {
        //TODO check required parameters
        this.fileName = fileName;
        this.domain = domain;
        this.content = content;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
    }

}
