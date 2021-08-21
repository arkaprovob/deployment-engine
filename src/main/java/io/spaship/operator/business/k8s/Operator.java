package io.spaship.operator.business.k8s;

import io.spaship.operator.business.Operations;
import io.spaship.operator.repo.SharedRepository;
import io.spaship.operator.type.SpashipMapping;
import io.spaship.operator.util.ReUsableItems;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class Operator implements Operations {
    private static final Logger LOG = LoggerFactory.getLogger(Operator.class);

    public Boolean createOrUpdateEnvironment(Triplet<SpashipMapping,UUID,String> inputParameters) {
        String websiteName = inputParameters.getValue0().getWebsiteName();
        ReUsableItems.enforceOpsLocking(new Pair<>(websiteName, inputParameters.getValue1()));
        ReUsableItems.blockFor(8000); //todo remove this, this is for ops simulation
        //todo add create or update
        return true;
    }



    public boolean isEnvironmentExists(String website,UUID uuid) {
        return false;
    }

    public Object createEnvironment(String website,UUID uuid){
        return null;
    }

    public Object updateSpa(String path){
        return null;
    }




}
