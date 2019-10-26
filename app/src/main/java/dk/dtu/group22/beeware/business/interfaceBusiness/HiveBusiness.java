package dk.dtu.group22.beeware.business.interfaceBusiness;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;

public interface HiveBusiness {

    // HiveBusiness hiveBusiness = new HiveBusinessImpl(new HiveRepoArrayListImpl());

    /**
     * @param user User must have id. For testing purposes user with id 1 should be used
     *             User user = new User();
     *             user.setId(1);
     * @param daysDelta number of days should be used be used to calculate a delta
     * @return overview of hives without detailed measurements.
     *         Hives includes however weight delta, and status values for weight, temp, humid, illum.
     */
    List<Hive> getHives(User user, int daysDelta);

    /**
     * @param hive Hive object, which must have valid id
     * @param fromTime Timestamp object. Will be used to fetch measurements since the given time.
     *                 new Timestamp(0) can be used to fetch all measurements
     * @param untilTime Timestamp object. Will be used to fetch measurements until the given time.
     *                  new Timestamp(System.currentTimeMillis()) can be used to fetch all measurements
     * @return Returns a hive object with all detailed measurements.
     */
    Hive getHive(Hive hive, Timestamp fromTime, Timestamp untilTime);

    void subscribeHive(User user, Hive hive);

    class HiveNotFound extends RuntimeException {
        public HiveNotFound(String msg) {super(msg);}
    }
}
