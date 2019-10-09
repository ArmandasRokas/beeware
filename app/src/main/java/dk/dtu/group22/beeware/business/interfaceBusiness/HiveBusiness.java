package dk.dtu.group22.beeware.business.interfaceBusiness;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;

public interface HiveBusiness {

    /**
     *
     * @param user
     * @param daysDelta number of days soulde be used to calculate a delta
     * @return A hive with the latest weight and weight delta
     */
    List<Hive> getHives(User user, int daysDelta);

    /**
     * @param hive Hive object, which must have valid id
     * @param fromTime Timestamp object. Will be used to fetch measurements since the given time.
     *                 new Timestamp(0) can be used to fetch all measurements
     * @param untilTime Timestamp object. Will be used to fetch measurements until the given time.
     *                  new Timestamp(System.currentTimeMillis()) can be used to fetch all measurements
     * @return Returns a hive object with all measurements.
     */
    Hive getHive(Hive hive, Timestamp fromTime, Timestamp untilTime);

    void subscribeHive(User user, Hive hive);
}
