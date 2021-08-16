package io.spaship.operator.service;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.operator.business.K8SOperator;
import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ApplicationScoped
public class SPAUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();
    private static final String SPASHIP_MAPPING_FILE = ".spaship";

    private final K8SOperator k8soperator;

    public SPAUploadHandler(K8SOperator k8soperator) {
        this.k8soperator = k8soperator;
    }

    /*
     * From Item build the file
     * Next mapping extract the .spaship file
     * Next POD,Configmaps,Service,Route,Ingress-Controller creation
     * */

    public void handleFileUpload(Pair<Path,String> input) {
        LOG.debug("Deployment process initiated");

        Uni.createFrom()
                .item(() -> spaMappingIntoMemory(input))
                .runSubscriptionOn(executor)
                .map(this::createOrUpdateEnvironment)
                .subscribe()
                .asCompletionStage()
                .whenComplete((c, e) -> {
                    if (!Objects.isNull(e))
                        LOG.error(e.getMessage());

                });

    }




    private Boolean createOrUpdateEnvironment(String spaship) {
        simulateTimeConsumingOps(); //TODO remove this block
        LOG.debug("task offloaded to operator");
        return k8soperator.createOrUpdateEnvironment(spaship);
    }


    private String spaMappingIntoMemory(Pair<Path,String> input) {
        Path absoluteFilePath = input.getValue0();
        LOG.debug("absolute absoluteFilePath is {}", absoluteFilePath);

        simulateTimeConsumingOps(); //TODO remove this block

        File spaDistribution = new File(absoluteFilePath.toUri());
        String spaMappingReference = null;

        assert spaDistribution.exists();

        try(ZipFile zipFile = new ZipFile(spaDistribution.getAbsolutePath())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            InputStream inputStream = readFromSpaMapping(zipFile, entries);

            Objects.requireNonNull(inputStream,SPASHIP_MAPPING_FILE+" not found");

            try(inputStream){
                spaMappingReference = IOUtils.toString(inputStream, Charset.defaultCharset());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return spaMappingReference;

    }

    private InputStream readFromSpaMapping(ZipFile zipFile, Enumeration<? extends ZipEntry> entries) {
        LOG.debug("reading from .spaship input stream");
        return Collections
                .list(entries)
                .stream()
                .filter(file -> file.getName().equals(SPASHIP_MAPPING_FILE))
                .findFirst().map(entry -> {
                    try {
                        LOG.debug("file loaded into memory");
                        return zipFile.getInputStream(entry);
                    } catch (IOException e) {
                        LOG.error("failed to load content into input-stream");
                        return null;
                    }
                }).orElse(null);
    }


    private void simulateTimeConsumingOps() {
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

}
