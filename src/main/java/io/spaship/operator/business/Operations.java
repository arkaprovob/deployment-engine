package io.spaship.operator.business;

import io.spaship.operator.type.SpashipMapping;
import org.javatuples.Triplet;

import java.util.UUID;

public interface Operations {

    Boolean createOrUpdateEnvironment(Triplet<SpashipMapping, UUID, String> inputParameters);

    boolean isEnvironmentExists(String website, UUID uuid);

    Object createEnvironment(String website, UUID uuid);

    Object updateSpa(String path);

}
