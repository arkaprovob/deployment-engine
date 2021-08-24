package io.spaship.operator.service;

import org.javatuples.Quartet;
import org.javatuples.Quintet;

import java.util.UUID;

public interface Operations {

    String createOrUpdateEnvironment(Quintet<String, UUID, String,String,String> inputParameters);


}
