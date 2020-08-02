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
}
