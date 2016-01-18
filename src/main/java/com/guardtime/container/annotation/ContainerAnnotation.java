package com.guardtime.container.annotation;

import java.io.InputStream;

public interface ContainerAnnotation {

    // TODO type. removable or not.

    String getFileName();

    String getDomain();

    InputStream getInputStream();

}
