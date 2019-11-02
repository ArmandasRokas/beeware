package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.util.List;

import dk.dtu.group22.beeware.dal.dao.Hive;

public interface ISubscription {
    List<Hive> getHivesToSubscribe();

    class UnableToFetchData extends RuntimeException {
        public UnableToFetchData(String msg) {super(msg);}
    }
}
