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
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoArrayListImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.UserRepoArrayListImpl;

public class HiveBusinessImpl implements HiveBusiness {

    private HiveRepository hiveRepo;
    private UserRepository userRepository;

    public HiveBusinessImpl(){
        this.hiveRepo = new HiveRepoArrayListImpl();
        this.userRepository = new UserRepoArrayListImpl();
    }

    @Override
    public List<Hive> getHives(User user, int daysDelta) {
        // TODO implement daysDelta. Armandas
        List<Hive> subscribedHives = userRepository.getSubscribedHives(user);
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for(Hive hive: subscribedHives){
            // TODO implement timestamp. Armandas
            Hive h = hiveRepo.getHive(hive, new Timestamp(0), new Timestamp(1570195921501L+1000));
            hivesWithMeasurements.add(h);
        }
        return hivesWithMeasurements;
    }

    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        return hiveRepo.getHive(hive, sinceTime, untilTime);
    }

    @Override
    public void subscribeHive(User user, Hive hive) {
        userRepository.subscribeHive(user, hive);
    }

    public UserRepository getUserRepository(){
        return this.userRepository;
    }
}