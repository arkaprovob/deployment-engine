package io.spaship.operator.service.k8s;

import io.spaship.operator.type.OperationResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.multipart.MultipartForm;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SideCarOperations {
    private static final Logger LOG = LoggerFactory.getLogger(SideCarOperations.class);
    private final Vertx vertx;
    private final WebClient client;

    public SideCarOperations(Vertx vertx) {
        this.vertx = vertx;
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("spaship-operator/0.0.1");
        this.client = WebClient.create(vertx, options);
    }

    @SneakyThrows
    public OperationResponse createOrUpdateSPDirectory(OperationResponse operationResponse) {
        var sideCarUrl = operationResponse.getSideCarServiceUrl().replace("tcp", "http");
        var environment = operationResponse.getEnvironment();
        LOG.info("sidecar url {} invoked with the following details {}", sideCarUrl, environment);
        environment.setOperationPerformed(true);

        MultipartForm form = MultipartForm.create()
                .textFileUpload("spa", operationResponse.spaName(), operationResponse.filePath().toAbsolutePath().toString(), "application/zip");

        return client.post("localhost").port(8081).uri("/api/upload").sendMultipartForm(form)
                .map(item -> {

                    LOG.debug("status code {} and status message {}", item.statusCode(), item.statusMessage());
                    OperationResponse response = item.bodyAsJson(OperationResponse.class);
                    LOG.debug("response is {}", response);
                    return response;
                })
                .onFailure().recoverWithItem(e -> OperationResponse.builder().environment(environment).sideCarServiceUrl(operationResponse.getSideCarServiceUrl()).status(0).originatedFrom(this.getClass().toString()).errorMessage(e.getMessage()).build())
                .subscribeAsCompletionStage().get();
    }


}
