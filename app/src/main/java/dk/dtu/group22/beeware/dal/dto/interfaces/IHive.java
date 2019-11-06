package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.sql.Timestamp;

import dk.dtu.group22.beeware.dal.dao.Hive;

public interface IHive {
    /***
     * @param hive The hive must have an ID
     * @param sinceTime retrieves measurements for a hive after the given timestamp.
     * @param untilTime retrieves measurements for a hive until the given timestamp.
     * @return A hive with measurements recorded between sinceTime and untilTime.
     */
    Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime);

}
