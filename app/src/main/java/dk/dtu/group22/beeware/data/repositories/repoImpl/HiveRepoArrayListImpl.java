package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

public class HiveRepoArrayListImpl implements HiveRepository {

    private static List<Integer> subscribeHives = new ArrayList<>();
    private static List<Hive> hiveList = new ArrayList<>();
    private static final int MINUTE_SUBTRACTION = 60000;
    static final long CURR_TIME = 1570195921501L;

    static {
        // dummy subscriptions
        subscribeHives.add(102);
        subscribeHives.add(103);

        // dummy hives
        Measurement meas1 = new Measurement();
        meas1.setTimestamp(new Timestamp(CURR_TIME));
        meas1.setWeight(32.0);
        meas1.setTempIn(35.0);
        meas1.setHumidity(98.9);
        meas1.setIlluminance(50000);

        Measurement meas2 = new Measurement();
        meas2.setTimestamp(new Timestamp(CURR_TIME - MINUTE_SUBTRACTION));
        meas2.setWeight(31.9);
        meas2.setTempIn(35.1);
        meas2.setHumidity(99.0);
        meas2.setIlluminance(49900);

        List<Measurement> measurements = new ArrayList<>();
        measurements.add(meas1);
        measurements.add(meas2);

        Hive test1 = new Hive();
        test1.setId(102);
        test1.setName("FHA_Stade102");
        test1.setMeasurements(measurements);
        hiveList.add(test1);
    }

    public void cleanSubscribedHives(){
        subscribeHives  = new ArrayList<>();
    }
    @Override
    public List<Hive> getHives(int userId) {
        return null;
    }
    // TODO : implement timestams og throws
    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        for (int i = 0; i < hiveList.size(); i++) {
            if(hive.getId() == hiveList.get(i).getId()){
                return hiveList.get(i);
            }
        }
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
        for (int hiveId : subscribeHives) {
            if (hive.getId() == hiveId) {
                throw new HiveIdAlreadyExists("Hive id already exists");
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
