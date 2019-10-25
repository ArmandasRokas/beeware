package dk.dtu.group22.beeware.data.repositories.interfaceRepo;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public interface HiveRepository {
    /***
     * @param hive The hive must have an ID
     * @param sinceTime retrieves measurements for a hive after the given timestamp.
     * @param untilTime retrieves measurements for a hive until the given timestamp.
     * @return A hive with measurements recorded between sinceTime and untilTime.
     */
    Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime);





}
