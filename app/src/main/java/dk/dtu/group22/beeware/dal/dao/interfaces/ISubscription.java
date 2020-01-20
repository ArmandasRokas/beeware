package dk.dtu.group22.beeware.dal.dao.interfaces;

import java.util.List;

import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

public interface ISubscription {
    List<NameIdPair> getHivesToSubscribe();

    class UnableToFetchData extends RuntimeException {
        public UnableToFetchData(String msg) {
            super(msg);
        }
    }

}
