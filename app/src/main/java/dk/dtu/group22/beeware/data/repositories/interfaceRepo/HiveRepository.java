package dk.dtu.group22.beeware.data.repositories.interfaceRepo;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;

public interface HiveRepository {

    /***
     * @param userId The id of the authenticated user
     * @return returns all hives with only latest/current measurements
     */

    List<Hive> getHives(int userId);

    /***
     * @param hiveId The id of a hive
     * @param sinceTime retrieves measurements for a hive after the given timestamp.
     * @param untilTime retrieves measurements for a hive until the given timestamp.
     * @return A hive with measurements recorded between sinceTime and untilTime.
     */
    Hive getHive(int hiveId, Timestamp sinceTime, Timestamp untilTime);

}
