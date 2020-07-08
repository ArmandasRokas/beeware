package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.util.List;

import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

public interface ISubscriptionManager {

    void saveSubscription(int id);

    List<Integer> getSubscriptions();

    void deleteSubscription(int id);

    /**
     * Cached hive names is used for an error messaging, when the app
     * only knows, which hiveId had problems.
     * @param hivesIdName
     */
    void cacheHivesToSub(List<NameIdPair> hivesIdName);

    String getCachedHiveName(int hiveId);
}
