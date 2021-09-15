package io.spaship.operator.service.k8s;

import io.spaship.operator.type.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SideCarOperations {
    private static final Logger LOG = LoggerFactory.getLogger(SideCarOperations.class);

    public OperationResponse createOrUpdateSPDirectory(OperationResponse operationResponse) {
        var sideCarUrl = operationResponse.getSideCarServiceUrl().replace("tcp", "http");
        var environment = operationResponse.getEnvironment();
        LOG.info("sidecar url {} invoked with the following details {}", sideCarUrl, environment);
        environment.setOperationPerformed(true);

        return OperationResponse.builder()
                .environment(environment).sideCarServiceUrl(operationResponse.getSideCarServiceUrl()).status(0)
                .originatedFrom(this.getClass()).errorMessage("feature not implemented").build();
    }


}
