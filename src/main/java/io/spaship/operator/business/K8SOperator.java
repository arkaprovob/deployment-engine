package io.spaship.operator.business;

import io.spaship.operator.repo.SharedRepository;
import io.spaship.operator.util.ReUsableItems;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class K8SOperator {
    private static final Logger LOG = LoggerFactory.getLogger(K8SOperator.class);

    public Boolean createOrUpdateEnvironment(Triplet<JsonObject,UUID,String> inputParameters) {
        String websiteName = inputParameters.getValue0().getString(
                ReUsableItems.getSpaMappingWebsiteNameAttribute()
        );
        enforceOpsLocking(new Pair<>(websiteName, inputParameters.getValue1()));
        ReUsableItems.blockFor(8000);
        return true;
    }



    private boolean isPodExist(String website,UUID uuid) {
        return false;
    }

    private Object createEnvironment(String website,UUID uuid){
        return null;
    }

    private Object updateSpa(String path){
        return null;
    }



    private void enforceOpsLocking(Pair<String, UUID> blockDecisionFactors) {
        while (blockCall(blockDecisionFactors)) {
            LOG.debug("An environment creation/modification is in progress for this website {}",
                    blockDecisionFactors.getValue0());
            ReUsableItems.blockFor(800);
        }
        SharedRepository.enqueue(blockDecisionFactors.getValue0(),
                new Pair<>(blockDecisionFactors.getValue1(), LocalDateTime.now()));
    }


    private boolean blockCall(Pair<String, UUID> decisionFactors) {
        String websiteName = decisionFactors.getValue0();
        UUID traceId = decisionFactors.getValue1();
        Pair<UUID, LocalDateTime> mapValue = SharedRepository.getEnvironmentLockMeta(websiteName);
        if (Objects.isNull(mapValue)) {
            LOG.warn("environmentLock not found!");
            return false;
        }
        LOG.debug("comparing {} with {}", decisionFactors, mapValue);

        return !SharedRepository.isQueued(websiteName)
                || !mapValue.getValue0().equals(traceId);
    }
}
