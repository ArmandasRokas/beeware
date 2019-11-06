package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.io.IOException;
import java.util.Map;

public interface ISubscriptionManager {

    void saveSubscription(int id) throws IOException;

    Map<String, ?> getSubscriptions() throws IOException;

    void deleteSubscription(int id) throws IOException;

}
