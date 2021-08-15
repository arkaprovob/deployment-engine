package io.spaship.operator.service;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;

@ApplicationScoped
public class SPAUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();

    /*
     * From Item build the file
     * Next mapping extract the .spaship file
     * Next POD,Configmaps,Service,Route,Ingress-Controller creation
     * */

    public void handleFileUpload(Path absoluteFilePath) {
        LOG.debug("Deployment process initiated");

        Uni.createFrom().item(() -> {
            pathToFile(absoluteFilePath);
            simulateTimeConsumingOps(); //TODO remove this block
            return "success";
        }).runSubscriptionOn(executor)
                .subscribe()
                .asCompletionStage()
                .whenComplete((c, e) -> {
                    if (!Objects.isNull(e))
                        LOG.error(e.getMessage());
                });

    }

    private void simulateTimeConsumingOps() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void pathToFile(Path absoluteFilePath) {
        LOG.debug("absolute absoluteFilePath is {}", absoluteFilePath);

        File spaDistribution = new File(absoluteFilePath.toUri());
        assert spaDistribution.exists();

        try {
            FileSystems.newFileSystem(Path.of(spaDistribution.getCanonicalPath())).getRootDirectories().forEach(root -> {
                LOG.debug(root.getFileName().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


/*        try {

        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }


}
