package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.util.ArrayList;

public interface ISubscriptionManager {

    void saveSubscription(int id);

    ArrayList<Integer> getSubscriptions();

    void deleteSubscription(int id);

}
