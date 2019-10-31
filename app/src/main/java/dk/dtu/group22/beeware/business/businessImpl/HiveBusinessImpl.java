package dk.dtu.group22.beeware.business.businessImpl;

import android.widget.ArrayAdapter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveSubscriptionRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoArrayListImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveSubscriptionRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveSubscriptionsRepoArrayListImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.UserRepoArrayListImpl;

public class HiveBusinessImpl implements HiveBusiness {

    private HiveRepository hiveRepo;
    private UserRepository userRepository;
    private HiveSubscriptionRepository hiveSubscriptionRepository;

    public HiveBusinessImpl(){
        this.hiveRepo = new HiveRepoHiveToolImpl();
        this.userRepository = new UserRepoArrayListImpl();
        this.hiveSubscriptionRepository = new HiveSubscriptionsRepoArrayListImpl();
    }

    @Override
    public List<Hive> getHives(User user, int daysDelta) {
        // TODO implement daysDelta. Armandas
        List<Hive> subscribedHives = userRepository.getSubscribedHives(user);
        /* // TODO uncomment these then getHive is implemented
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for(Hive hive: subscribedHives){
            // TODO implement timestamp. Armandas
            Hive h = hiveRepo.getHive(hive, new Timestamp(0), new Timestamp(1570195921501L+1000));
            if (h == null){
                throw new HiveNotFound("Hive with id " + hive.getId() + " does not exits.");
            } else {
                hivesWithMeasurements.add(h);
            }
        }
        return hivesWithMeasurements;
        */
        return subscribedHives;
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
