package io.spaship.operator.business;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.operator.repo.SharedRepository;
import io.spaship.operator.service.k8s.Operator;
import io.spaship.operator.type.Environment;
import io.spaship.operator.type.SpashipMapping;
import io.spaship.operator.util.ReUsableItems;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ApplicationScoped
public class SPAUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();
    private final Operator k8soperator;
    private final String nameSpace;


    public SPAUploadHandler(Operator k8soperator, @Named("namespace") String nameSpace) {
        this.k8soperator = k8soperator;
        this.nameSpace = nameSpace;
    }

    private static Environment createEnvironmentObject(JsonObject entries) {
        return null;
    }

    //[0]file-store-path[1]ops-tracing-id[2]website-name
    public void handleFileUpload(Triplet<Path, UUID, String> input) {
        LOG.debug("     deployment process initiated with details {}", input);

        Uni.createFrom()
                .item(() -> spaMappingIntoMemory(input)) //input->[0]file-store-path[1]ops-tracing-id[2]website-name
                .runSubscriptionOn(executor)
                .map(this::buildEnvironmentList)//input->[0]SpashipMapping-object[1]ops-tracing-id[2]zipfile path
                //.map(inputParameters -> createOrUpdateEnvironment(inputParameters))
                .subscribe()
                .asCompletionStage()
                .whenComplete((result, exception) -> {
                    LOG.debug("     operation completed with details {}", input);
                    if (!Objects.isNull(exception))
                        LOG.error(exception.getMessage());
                    SharedRepository.dequeue(input.getValue2());
                });

    }

    private Triplet<String, UUID, Path> spaMappingIntoMemory(Triplet<Path, UUID, String> input) {
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

        var output = new Triplet<>(spaMappingReference, input.getValue1(), input.getValue0());
        LOG.debug("output of spaMappingIntoMemory  {} ", output);
        return output;
    }

    private List<Environment> buildEnvironmentList(Triplet<String, UUID, Path> input) {
        SpashipMapping spaMapping = new SpashipMapping(input.getValue0());

        var environments = spaMapping.getEnvironments();
        var environmentSize = environments.size();
        LOG.debug("{} no of environments detected and first entry is {}", environmentSize, environments.get(0));

        List<Environment> allEnvironments = environments.stream()
                .map(environmentMapping -> {

                    var envName = environmentMapping.getString("name");
                    var websiteName = spaMapping.getString("websiteName");
                    var traceID = input.getValue1();
                    var updateRestriction = environmentMapping.getBoolean("updateRestriction");
                    var zipFileLocation = input.getValue2();
                    var websiteVersion = spaMapping.getWebsiteVersion();
                    var spaName = spaMapping.getName();
                    var spaContextPath = spaMapping.getContextPath();
                    var branch = spaMapping.getBranch();
                    var excludeFromEnvironment = environmentMapping.getBoolean("exclude");

                    Environment environment = new Environment(envName, websiteName, traceID, this.nameSpace, updateRestriction, zipFileLocation,
                            websiteVersion, spaName, spaContextPath, branch, excludeFromEnvironment);
                    LOG.debug("Constructed environment object is {}", environment);
                    return environment;

                })
                .collect(Collectors.toList());

        assert environmentSize == allEnvironments.size();

        return allEnvironments;
    }

    //[2]environment[3]nameSpace
    private String createOrUpdateEnvironment(Quartet<SpashipMapping, UUID, String, String> inputParameters) {
        LOG.debug("offloading task to the operator");
        String websiteName = inputParameters.getValue0().getWebsiteName();
        UUID uuid = inputParameters.getValue1();
        String environment = inputParameters.getValue2();
        String ns = inputParameters.getValue3();
        var input = new Quartet<>(websiteName, uuid, environment, ns);
        return null;//k8soperator.createOrUpdateEnvironment(input);
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

/*    List<String> environments =  spaMapping.getEnvironments();
    List<String> excludedEnvironments = spaMapping.getExcludeFromEnvs();
        if(!excludedEnvironments.isEmpty())
    deleteEnvironments(spaMapping,uuid,namespace);
        environments.removeAll(excludedEnvironments);

        environments.stream().map(env->{
        return null;
    }).collect(Collectors.toMap());*/

}
