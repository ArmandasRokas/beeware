package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.util.List;

public interface ISubscription {
    List<NameIdPair> getHivesToSubscribe();

    class UnableToFetchData extends RuntimeException {
        public UnableToFetchData(String msg) {super(msg);}
    }
}

