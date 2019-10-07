package dk.dtu.group22.beeware.business.interfaceBusiness;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public interface HiveBusiness {

    /**
     *
     * @param user
     * @param daysDelta number of days soulde be used to calculate a delta
     * @return A hive with the latest weight and weight delta
     */
    List<Hive> getHives(User user, int daysDelta);

    void subscribeHive(User user, Hive hive);
}
