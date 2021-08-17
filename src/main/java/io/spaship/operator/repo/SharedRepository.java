package io.spaship.operator.repo;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

public class SharedRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SharedRepository.class);
    // This is used to prevent concurrency issues for environment creation, so that one environment or setup does not get created when
    private static final Map<String, Pair<UUID, LocalDateTime>> environmentLock = Collections.synchronizedMap(new HashMap<>());

    private SharedRepository() {
    }

    public static boolean enqueue(String websiteName, Pair<UUID, LocalDateTime> meta) {
        if (isQueued(websiteName)) {
            LOG.debug("entry {} already exists",websiteName);
            return false;
        }
        LOG.debug("enqueuing the following details {} of website {}",meta,websiteName);
        environmentLock.put(websiteName, meta);
        return true;
    }

    public static boolean dequeue(String websiteName) {
        var value = environmentLock.remove(websiteName);
        LOG.debug("dequeued the following details {} of website {}",value,websiteName);
        return !Objects.isNull(value);
    }

    public static boolean isQueued(String websiteName) {
        return !Objects.isNull(environmentLock.get(websiteName));
    }

    public static Pair<UUID, LocalDateTime> getEnvironmentLockMeta(String website){
        var value = environmentLock.get(website);
        LOG.debug("value against entry {} is {}",website,value);
        return value;
    }
}
