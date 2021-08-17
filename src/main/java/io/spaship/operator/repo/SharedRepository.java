package io.spaship.operator.repo;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SharedRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SharedRepository.class);
    // This is used to prevent concurrency issues for environment creation, so that one environment or setup does not get created when
    private static final Map<String, Pair<String, LocalDateTime>> environmentLock = Collections.synchronizedMap(new HashMap<>());

    private SharedRepository() {
    }

    public static boolean enqueue(String websiteName, Pair<String, LocalDateTime> meta) {
        if (isQueued(websiteName)) {
            LOG.debug("website already exists");
            return false;
        }
        LOG.debug("website not found, adding ino the list");
        environmentLock.put(websiteName, meta);
        return true;
    }

    public static boolean dequeue(String websiteName) {
        var value = environmentLock.remove(websiteName);
        return !Objects.isNull(value);
    }

    public static boolean isQueued(String websiteName) {
        return !Objects.isNull(environmentLock.get(websiteName));
    }
}
