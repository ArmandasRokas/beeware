package dk.dtu.group22.beeware.dal.dto.implementation;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.interfaces.IUser;

public class UserHiveIds implements IUser {

    private static List<Integer> subscribedIds = new ArrayList<>();

    public void cleanSubscribedHives() {
        subscribedIds = new ArrayList<>();
    }

    public void subscribeHive(int id) {
        if (id < 1) {
            throw new IUser.HiveNoIdException("Hive id is not defined");
        }
        if (subscribedIds.contains(id)) {
            throw new IUser.HiveIdAlreadyExists("Hive id already exists");
        }
        subscribedIds.add(id);
    }


    // Returns a copy as to make subscribedIds safe
    public List<Integer> getSubscribedIds() {
        return new ArrayList<>(subscribedIds);
    }

    public void unsubscribeHive(int id) {
        if (id < 1) {
            throw new IUser.HiveNoIdException("Hive id is not defined");
        }
        subscribedIds.remove(new Integer(id));
    }

    public void subscribeHivesFromList(List<Integer> ids) {
        for (int id : ids) {
            subscribeHive(id);
        }
    }

    public void subscribeHivesFromCSString(String ids) {
        String[] rawIds = ids.split(",");
        for (String id : rawIds) {
            try {
                subscribeHive(Integer.parseInt(id));
            } catch (Exception e) {
                throw new IllegalArgumentException("Ill formatted string");
            }
        }
    }

}
