package dk.dtu.group22.beeware.data.repositories.interfaceRepo;

import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;

public interface HiveSubscriptionRepository {
    List<Hive> getHivesToSubscribe();

    class UnableToFetchData extends RuntimeException {
        public UnableToFetchData(String msg) {super(msg);}
    }
}
