package io.spaship.operator.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@QuarkusTest
class SPAUploadHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandlerTest.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    private void pathToFile() throws URISyntaxException, IOException {

        File spaDistribution = new File("/home/arkaprovo/IdeaProjects/spa-deployment-operator/src/test/resources/home-spa.zip");
        assert spaDistribution.exists();
        ZipFile zipFile = new ZipFile(spaDistribution.getAbsolutePath());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        int counter = 0;

        while (entries.hasMoreElements()) {
            counter++;
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
        }
        LOG.debug("total {} no of items found", counter);

    }

    @Test
    void readZipFile() throws URISyntaxException, IOException {
        pathToFile();
        assert true == true;
    }
}