package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public interface CachedHiveRepoI {

    /**
     * @param hiveId valid hive id
     * @return returns hive with all meta data and measurements
     */
    Hive getCachedHiveWithAllData(int hiveId);

    /**
     * Creates a new hive in cache
     * @param hive hive with all meta data and measurements
     */
    void createCachedHive(Hive hive);

    /**
     * Updates only hive meta data, but not measurements.
     * In order to store new measurements use
     * saveNewMeasurements(Hive hive, List<Measurement> measurements)
     * @param hive updated hive
     */
    void updateHiveMetaData(Hive hive);

    /**
     * Use this method to cache new measurements which
     * has not been cached before.
     * @param hive Hive with id in order to identify hive
     * @param measurements new measurements which need to be cached
     */
    void saveNewMeasurements(Hive hive, List<Measurement> measurements);

    /**
     * @param id valid hive id
     * @param since since timestamp
     * @param until until timestamp
     * @return hive with meta data and measurements within since and until
     */
    Hive getHiveWithinPeriod(int id, Timestamp since, Timestamp until);

    /**
     * Use this method when you don't know which period of a hive was
     * cached.
     * @param hiveId valid hive id.
     * @return returns List<Measurement> of two elements.
     * Index 0: a measurement with the minimum timestamp in the cache
     * Index 1: a measurement with the maximum timestamp in the cache
     */
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
