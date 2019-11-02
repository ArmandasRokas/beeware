package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.util.List;

import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.User;

public interface IUser {
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
