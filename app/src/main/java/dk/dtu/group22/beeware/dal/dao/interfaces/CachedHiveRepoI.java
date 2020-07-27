package dk.dtu.group22.beeware.dal.dao.interfaces;

import dk.dtu.group22.beeware.dal.dto.Hive;

public interface CachedHiveRepoI {

    Hive getCachedHiveWithAllData(int hiveId);

    void createCachedHive(Hive hive);
}
