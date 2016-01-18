package com.guardtime.container.datafile;

import java.io.InputStream;

public interface ContainerDataFile {

    String getFileName();

    InputStream getInputStream();

}
