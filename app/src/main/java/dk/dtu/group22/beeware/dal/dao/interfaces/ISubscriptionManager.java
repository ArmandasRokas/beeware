package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.util.List;

public interface ISubscriptionManager {

    void saveSubscription(int id);

    List<Integer> getSubscriptions();

    void deleteSubscription(int id);

}
