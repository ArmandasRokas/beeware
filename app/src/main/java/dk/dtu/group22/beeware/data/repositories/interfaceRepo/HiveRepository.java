package dk.dtu.group22.beeware.data.repositories.interfaceRepo;

import java.sql.Timestamp;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public interface HiveRepository {




    /***
     * @param hiveId The id of a hive
     * @param sinceTime retrieves measurements for a hive after the given timestamp.
     * @param untilTime retrieves measurements for a hive until the given timestamp.
     * @return A hive with measurements recorded between sinceTime and untilTime.
     */
    Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime);

    void subscribeHive(User user, Hive hive) throws UserNoIdException, HiveNoIdException;

    /**
     * @param user The user must have id
     * @return
     * @throws UserNoIdException
     */
    List<Hive> getSubscribedHives(User user) throws UserNoIdException;

    void unsubscribeHive(User user, Hive hive);

    class UserNoIdException extends RuntimeException {
        public UserNoIdException(String msg) {super(msg);}
    }

    class HiveNoIdException extends RuntimeException {
        public HiveNoIdException(String msg) {super(msg);}
    }

    class HiveIdAlreadyExists extends RuntimeException {
        public HiveIdAlreadyExists(String msg) {super(msg);}
    }

}
