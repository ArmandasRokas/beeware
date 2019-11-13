package dk.dtu.group22.beeware.dal.dto.interfaces;

import java.util.List;

public interface IUser {
    void subscribeHive(int id) throws UserNoIdException, HiveNoIdException;

    /**
     * @return A list of subscribed hives ids.
     * @throws UserNoIdException
     */
    List<Integer> getSubscribedIds() throws UserNoIdException;

    void unsubscribeHive(int id);

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
