package io.spaship.operator.service;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;

@ApplicationScoped
public class SPAUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();


    public void handleFileUpload(String fileName, URI fileURI,Path path){
        LOG.debug("Deployment process initiated");
        extractSpaMapping(fileURI,path);
        Uni.createFrom().item(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "success";
        }).runSubscriptionOn(executor)
                .subscribe()
                .asCompletionStage()
                .whenComplete((c,e)->{LOG.debug("Deployment complete... {}", Objects.isNull(e));});

    }

    private void extractSpaMapping(URI fromZip,Path path){
        LOG.debug("incoming URI is {}",fromZip);
        try {
            var staticUri = new URI("file:///tmp//operator//xe.zip");
            fromZip = staticUri;
            LOG.debug("after modification fromZip is {}",fromZip);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            FileSystems.newFileSystem(path).getRootDirectories().forEach(root->{LOG.debug(root.getFileName().toString());});
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




}
