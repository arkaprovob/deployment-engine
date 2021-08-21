package io.spaship.operator.service;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.operator.business.k8s.Operator;
import io.spaship.operator.repo.SharedRepository;
import io.spaship.operator.type.SpashipMapping;
import io.spaship.operator.util.ReUsableItems;
import org.apache.commons.io.IOUtils;
import org.javatuples.Triplet;
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
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ApplicationScoped
public class SPAUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();
    private final Operator k8soperator;

    public SPAUploadHandler(Operator k8soperator) {
        this.k8soperator = k8soperator;
    }

    /*
     * From Item build the file
     * Next mapping extract the .spaship file
     * Next POD,Configmaps,Service,Route,Ingress-Controller creation
     * */

    public void handleFileUpload(Triplet<Path, UUID, String> input) {
        LOG.debug("     deployment process initiated with details {}", input);

        Uni.createFrom()
                .item(() -> spaMappingIntoMemory(input))
                .runSubscriptionOn(executor)
                .map(this::stringToSpashipMapping)
                .map(this::createOrUpdateEnvironment)
                .subscribe()
                .asCompletionStage()
                .whenComplete((result, exception) -> {
                    LOG.debug("     operation completed with details {}", input);
                    if (!Objects.isNull(exception))
                        LOG.error(exception.getMessage());
                    SharedRepository.dequeue(input.getValue2());
                });

    }

    private Triplet<String, UUID, String> spaMappingIntoMemory(Triplet<Path, UUID, String> input) {
        Path absoluteFilePath = input.getValue0();
        LOG.debug("absolute absoluteFilePath is {}", absoluteFilePath);

        File spaDistribution = new File(absoluteFilePath.toUri());
        String spaMappingReference = null;
        assert spaDistribution.exists();
        try (ZipFile zipFile = new ZipFile(spaDistribution.getAbsolutePath())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            InputStream inputStream = readFromSpaMapping(zipFile, entries);
            Objects.requireNonNull(inputStream, ReUsableItems.getSpashipMappingFileName() + " not found");
            try (inputStream) {
                spaMappingReference = IOUtils.toString(inputStream, Charset.defaultCharset());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Triplet<>(spaMappingReference, input.getValue1(), input.getValue2());
    }


    private Triplet<SpashipMapping, UUID, String> stringToSpashipMapping(Triplet<String, UUID, String> input) {
        SpashipMapping spaMapping = new SpashipMapping(input.getValue0());
        return new Triplet<>(spaMapping, input.getValue1(), input.getValue2());
    }

    private Boolean createOrUpdateEnvironment(Triplet<SpashipMapping, UUID, String> inputParameters) {
        LOG.debug("offloading task to the operator");
        return k8soperator.createOrUpdateEnvironment(inputParameters);
    }


    private InputStream readFromSpaMapping(ZipFile zipFile, Enumeration<? extends ZipEntry> entries) {
        LOG.debug("reading from .spaship input stream");
        return Collections
                .list(entries)
                .stream()
                .filter(file -> file.getName().equals(ReUsableItems.getSpashipMappingFileName()))
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

}
