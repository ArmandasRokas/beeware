package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

public class HiveRepoArrayListImpl implements HiveRepository {
    @Override
    public List<Hive> getHives(int userId) {
        return null;
    }

    @Override
    public Hive getHive(int hiveId, Timestamp sinceTime, Timestamp untilTime) {

        return null;
    }

}
