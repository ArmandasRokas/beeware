package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.util.List;

import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

public interface ISubscriptionManager {

    void saveSubscription(int id);

    List<Integer> getSubscriptions();

    void deleteSubscription(int id);

    /**
     * Caches hives meta data for offline use.
     * @param hivesIdName
     */
    void cacheHivesToSub(List<NameIdPair> hivesIdName);

    //String getCachedHiveName(int hiveId);

    /**
     * Receives cached NameIdPairs of the hives
     * @param hiveId
     * @return
     */
    NameIdPair getCachedNameIdPair(int hiveId);
}
