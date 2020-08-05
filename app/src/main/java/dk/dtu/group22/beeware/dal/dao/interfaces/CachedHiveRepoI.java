package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public interface CachedHiveRepoI {

    Hive getCachedHiveWithAllData(int hiveId);

    void createCachedHive(Hive hive);

    void updateHive(Hive hive);

    void saveNewMeasurements(Hive hive, List<Measurement> measurements);

    Hive getHiveWithinPeriod(int id, Timestamp since, Timestamp until);

    List<Measurement> fetchMinMaxMeasurementsByTimestamp(int hiveId);

    /**
     * Fetches cached hive from (maxTimestamp - timeDelta) until maxTimestamp.
     * The method is used in order to get the most recent cached data when a device does not have connection
     * @param id The id of the hive
     * @param timeDelta In order to define the range of the recent data from the most recent measurement.
     *                  E.g. if you put 1000*60*60*24*7, the method will return the hive with the data 7 day back
     *                  from the most recent measurement.
     * @return Hive with most recent measurements in the defined range
     */
    Hive getHiveWithMostRecentData(int id, long timeDelta);
}
