package io.spaship.operator.service.k8s;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.spaship.operator.exception.NotImplementedException;
import io.spaship.operator.exception.ResourceNotFoundException;
import io.spaship.operator.service.Operations;
import io.spaship.operator.type.Environment;
import io.spaship.operator.type.OperationResponse;
import io.spaship.operator.util.ReUsableItems;
import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@ApplicationScoped
public class Operator implements Operations {
    private static final Logger LOG = LoggerFactory.getLogger(Operator.class);

    private final KubernetesClient k8sClient;

    public Operator(KubernetesClient k8sClient) {
        this.k8sClient = k8sClient;
    }


    public OperationResponse createOrUpdateEnvironment(Environment environment) {

        ReUsableItems.enforceOpsLocking(new Pair<>(environment.getIdentification(), environment.getTraceID()));
        boolean isNewEnvironment = environmentExists(environment);
        LOG.debug("isNewEnvironment is {}", isNewEnvironment);

        return null;
    }

    //website-name[0],uuid[1],environment[2],namespace[3],websiteVersion[4]

    public String oldCreateOrUpdateEnvironment(Quintet<String, UUID, String, String, String> inputParameters) {
        String websiteName = inputParameters.getValue0();
        ReUsableItems.enforceOpsLocking(new Pair<>(websiteName, inputParameters.getValue1()));
        boolean isNewEnvironment = false;//isEnvironmentExists(inputParameters).isEmpty();
        LOG.debug("isNewEnvironment is {}", isNewEnvironment);
        if (isNewEnvironment)
            createNewEnvironment(inputParameters);
        return environmentSidecarUrl(inputParameters);
    }

    void createNewEnvironment(Quintet<String, UUID, String, String, String> inputParameters) {
        Map<String, String> templateParameters = Map.of(
                "WEBSITE", inputParameters.getValue0(),
                "TRACE_ID", inputParameters.getValue1().toString(),
                "ENV", inputParameters.getValue2());
        LOG.debug("templateParameters are as follows {}", templateParameters);
        var result = ((OpenShiftClient) k8sClient)
                .templates()
                .inNamespace(inputParameters.getValue3())
                .load(Operations.class.getResourceAsStream("/openshift/environment-template.yaml"))
                .processLocally(templateParameters);
        processK8sList(result, inputParameters.getValue1(), inputParameters.getValue3());
    }

    private void processK8sList(KubernetesList result, UUID tracing, String nameSpace) {

        result.getItems().forEach(item -> {
            if (item instanceof Service) {
                LOG.debug("creating new Service in K8s, tracing = {}", tracing);
                k8sClient.services().inNamespace(nameSpace).createOrReplace((Service) item);
            }
            if (item instanceof Deployment) {
                LOG.debug("creating new Deployment in K8s, tracing = {}", tracing);
                k8sClient.apps().deployments().inNamespace(nameSpace).createOrReplace((Deployment) item);
            }
            if (item instanceof Route) {
                LOG.debug("creating new Route in K8s, tracing = {}", tracing);
                ((OpenShiftClient) k8sClient).routes().inNamespace(nameSpace).createOrReplace((Route) item);
            }
            LOG.debug("created resource in kubernetes, tracing = {}", tracing);
        });
    }


    public List<Pod> isEnvironmentExists(Environment environment) {
        Map<String, String> labels = searchCriteriaLabel(environment);
        List<Pod> matchedPods = k8sClient.pods().inNamespace(environment.getNameSpace()).withLabels(labels).list()
                .getItems();
        LOG.debug("{} no of matched pod found with search criteria {}", matchedPods.size(), labels);
        return matchedPods;
    }

    public boolean environmentExists(Environment environment) {
        Map<String, String> labels = searchCriteriaLabel(environment);
        List<Pod> matchedPods = k8sClient.pods().inNamespace(environment.getNameSpace()).withLabels(labels).list()
                .getItems();
        LOG.debug("{} no of matched pod found with search criteria {}", matchedPods.size(), labels);
        return !matchedPods.isEmpty();
    }


    public OperationResponse deleteEnvironment(Environment environment) {
        throw new NotImplementedException();
    }


    String environmentSidecarUrl(Quintet<String, UUID, String, String, String> inputParameters) {
        String serviceName = "svc"
                .concat("-")
                .concat(inputParameters.getValue0().toLowerCase())
                .concat("-")
                .concat(inputParameters.getValue2().toLowerCase())
                .concat("-")
                .concat(inputParameters.getValue4().toLowerCase());
        LOG.debug("computed service name is {}", serviceName);
        String url = k8sClient.services().inNamespace(inputParameters.getValue3())
                .withName(serviceName).getURL("http");
        if (Objects.isNull(url))
            throw new ResourceNotFoundException("service:" + serviceName);
        return url;
    }


    Map<String, String> searchCriteriaLabel(Environment environment) {
        return Map.of("managedBy", "spaship",
                "website", environment.getWebsiteName().toLowerCase(),
                "environment", environment.getName().toLowerCase(),
                "websiteVersion", environment.getWebsiteVersion().toLowerCase()
        );
    }
}
