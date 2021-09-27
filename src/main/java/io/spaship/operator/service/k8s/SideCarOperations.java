package io.spaship.operator.service.k8s;

import io.spaship.operator.business.EventManager;
import io.spaship.operator.type.EventStructure;
import io.spaship.operator.type.OperationResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.multipart.MultipartForm;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class SideCarOperations {
    private static final Logger LOG = LoggerFactory.getLogger(SideCarOperations.class);
    private final WebClient client;
    private final EventManager eventManager;

    public SideCarOperations(Vertx vertx, EventManager eventManager) {
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("spaship-operator/0.0.1");
        this.client = WebClient.create(vertx, options);

        this.eventManager = eventManager;
    }

    @SneakyThrows
    //TODO break into multiple methods
    public OperationResponse createOrUpdateSPDirectory(OperationResponse operationResponse) {
        var sideCarUrl = operationResponse.getSideCarServiceUrl().replace("tcp", "http");
        var environment = operationResponse.getEnvironment();
        LOG.info("sidecar url {} invoked with the following details {}", sideCarUrl, environment);
        environment.setOperationPerformed(true);
        var sideCarUrlPart = sideCarUrl.split(":");
        var host = "http://".concat(sideCarUrlPart[1].replace("//", ""));
        var port = sideCarUrlPart[2];

        MultipartForm form = MultipartForm.create()
                .textFileUpload("spa", operationResponse.spaName(), operationResponse.filePath().toAbsolutePath()
                        .toString(), "application/zip"
                );

        var responseOnFailure = OperationResponse.builder().environment(environment)
                .sideCarServiceUrl(operationResponse.getSideCarServiceUrl()).status(0)
                .originatedFrom(this.getClass().toString());

        var requestUri = host.concat(":").concat(port).concat("/api/upload");
        LOG.info("posting in  {}", requestUri);

        var opResp = client.requestAbs(HttpMethod.POST, requestUri).sendMultipartForm(form)
                .map(item -> apply(responseOnFailure, item))
                .onFailure()
                .recoverWithItem(e -> fallbackResponse(responseOnFailure, e))
                .subscribeAsCompletionStage()
                .get();

        eventManager.queue(
                EventStructure.builder()
                        .websiteName(opResp.getEnvironment().getWebsiteName())
                        .environmentName(opResp.getEnvironment().getName())
                        .uuid(operationResponse.getEnvironment().getTraceID())
                        .state(opResp.getErrorMessage() == null ?
                                "spa deployment ops performed" : "spa deployment ops failed")
                        .build());

        return opResp;
    }

    private OperationResponse fallbackResponse(OperationResponse.OperationResponseBuilder responseOnFailure, Throwable e) {
        LOG.error("failed to upload into sidecar container due to {}", e.getMessage());
        return responseOnFailure.errorMessage(e.getMessage()).build();
    }

    private OperationResponse apply(OperationResponse.OperationResponseBuilder responseOnFailure,
                                    HttpResponse<Buffer> item) {
        var responseFromSidecar = item.bodyAsJson(OperationResponse.class);
        if (Objects.isNull(responseFromSidecar))
            return responseOnFailure.errorMessage("status code: "
                    .concat(Integer.toString(item.statusCode()))
                    .concat(" message: ")
                    .concat(item.statusMessage())).build();
        LOG.debug("sidecar response if  {}", responseFromSidecar);
        return responseFromSidecar;
    }


}
