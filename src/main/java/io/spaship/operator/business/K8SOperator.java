package io.spaship.operator.business;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class K8SOperator {
    public Boolean createOrUpdateEnvironment(String spaship) {
        return true;
    }
}
