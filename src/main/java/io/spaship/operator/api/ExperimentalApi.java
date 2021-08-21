package io.spaship.operator.api;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

@Path("test")
public class ExperimentalApi {

    @Inject
    @Channel("book-out")
    Multi<String> books;


    @GET
    @Path("/pods/{value}")
    @Produces("text/plain")
    public Uni<List<Pod>> listOfPods(@PathParam("value") String value){
        List<Pod> pods= null;
        try (KubernetesClient client = new DefaultOpenShiftClient()) {

            var po = client.pods().inNamespace("spaship").withLabel("app.kubernetes.io/name",value);
            pods = po.list().getItems();
            po.delete();
            client.configMaps().inNamespace("").createOrReplace();
            client.pods().inNamespace("").withName("").delete();
            var informer = client.informers().getExistingSharedIndexInformer(Pod.class);
            informer.addEventHandler(new ResourceEventHandler<Pod>() {
                @Override
                public void onAdd(Pod obj) {

                }

                @Override
                public void onUpdate(Pod oldObj, Pod newObj) {

                }

                @Override
                public void onDelete(Pod obj, boolean deletedFinalStateUnknown) {

                }
            });

        } catch (KubernetesClientException ex) {
            // Handle exception
            ex.printStackTrace();
        }
        return Uni.createFrom().item(pods);
    }


}
