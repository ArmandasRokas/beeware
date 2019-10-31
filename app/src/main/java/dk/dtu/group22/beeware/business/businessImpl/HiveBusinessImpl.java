package dk.dtu.group22.beeware.business.businessImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveSubscriptionRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveSubscriptionRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.UserRepoArrayListImpl;

public class HiveBusinessImpl implements HiveBusiness {

    private HiveRepository hiveRepo;
    private UserRepository userRepository;
    private HiveSubscriptionRepository hiveSubscriptionRepository;

    public HiveBusinessImpl(){
        this.hiveRepo = new HiveRepoHiveToolImpl();
        this.userRepository = new UserRepoArrayListImpl();
        this.hiveSubscriptionRepository = new HiveSubscriptionRepoHiveToolImpl();
    }

    @Override
    public List<Hive> getHives(User user, int daysDelta) {
        // TODO implement daysDelta. Armandas. Days delta is how many days back needs to fetched from today
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Hive> subscribedHives = userRepository.getSubscribedHives(user);
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for(Hive hive: subscribedHives){
            Hive h = hiveRepo.getHive(hive, new Timestamp(since), new Timestamp(now));
            if (h == null){
                throw new HiveNotFound("Hive with id " + hive.getId() + " does not exits.");
            } else {
                h = calculateCurrValuesAndStatus(h);
                hivesWithMeasurements.add(h);
            }


        }
        return hivesWithMeasurements;
    }
    private Hive calculateCurrValuesAndStatus(Hive hive){
        hive.setCurrWeight(hive.getMeasurements().get(hive.getMeasurements().size()-1).getWeight());
        System.out.println(hive.getMeasurements().get(0).getTimestamp().toString().substring(8,10));
        // TODO calculate delta. Idea loop over array of measurments and when day is changed. (the day of curr measurment is not equal to the day of next measurment)
        return hive;
    }

    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        return hiveRepo.getHive(hive, sinceTime, untilTime);
    }

    @Override
    public void subscribeHive(User user, Hive hive) {
        try{
            userRepository.subscribeHive(user, hive);
        } catch (Exception e){
            // TODO add exception handling
            e.printStackTrace();
        }
    }

    @Override
    public List<Hive> getHivesToSubscribe() {
        try{
            List<Hive> hives = hiveSubscriptionRepository.getHivesToSubscribe();
            if(hives == null || hives.isEmpty()){
                throw new HivesToSubscribeNoFound("Business error. Unable to fetch data");
            } else{
                return hives;
            }
        } catch (Exception e){
            throw new HivesToSubscribeNoFound(e.getMessage());
        }
    }

    public UserRepository getUserRepository(){
        return this.userRepository;
    }
}
