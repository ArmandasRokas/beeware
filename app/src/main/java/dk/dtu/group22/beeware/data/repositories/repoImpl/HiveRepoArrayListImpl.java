package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

public class HiveRepoArrayListImpl implements HiveRepository {

    private static List<Integer> subscribeHives = new ArrayList<>();


    static {
        subscribeHives.add(102);
        subscribeHives.add(103);
    }

    public void cleanSubscribedHives(){
        subscribeHives  = new ArrayList<>();
    }
    @Override
    public List<Hive> getHives(int userId) {
        return null;
    }

    @Override
    public Hive getHive(int hiveId, Timestamp sinceTime, Timestamp untilTime) {

        return null;
    }

    @Override
    public void subscribeHive(User user, Hive hive) {
        if(user.getId() < 1){
            throw new UserNoIdException("User id is not defined");
        }
        if(hive.getId() < 1){
            throw new HiveNoIdException("Hive id is not defined");
        }
        subscribeHives.add(hive.getId());
    }

    @Override
    public List<Hive> getSubscribedHives(User user) {
        if(user.getId() < 1){
            throw new UserNoIdException("User id is not defined");
        }
        List<Hive> hives = new ArrayList<>();
        for(int hiveId: subscribeHives){
            if(hiveId > 0){
                Hive hive = new Hive();
                hive.setId(hiveId);
                hives.add(hive);
            }
        }
        return hives;
    }
}
