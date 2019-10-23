package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;

public class UserRepoArrayListImpl implements UserRepository{

    private static List<Integer> subscribeHives = new ArrayList<>();

    static {
        // dummy subscriptions
        subscribeHives.add(102);
        subscribeHives.add(103);
        subscribeHives.add(104);
    }

    public void cleanSubscribedHives(){
        subscribeHives  = new ArrayList<>();
        //hiveList = new ArrayList<>();
    }

    @Override
    public void subscribeHive(User user, Hive hive) {
        if(user.getId() < 1){
            throw new UserRepository.UserNoIdException("User id is not defined");
        }
        if(hive.getId() < 1){
            throw new UserRepository.HiveNoIdException("Hive id is not defined");
        }
        for (int hiveId : subscribeHives) {
            if (hive.getId() == hiveId) {
                throw new UserRepository.HiveIdAlreadyExists("Hive id already exists");
            }
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

    @Override
    public void unsubscribeHive(User user, Hive hive) {
        if(user.getId() < 1){
            throw new UserNoIdException("User id is not defined");
        }
        if(hive.getId() < 1){
            throw new HiveNoIdException("Hive id is not defined");
        }
        for (int i = 0; i < subscribeHives.size(); i++) {
            if (subscribeHives.get(i) == hive.getId()) {
                subscribeHives.remove(i);
            }
        }
    }

}
