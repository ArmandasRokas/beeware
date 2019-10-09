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
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoArrayListImpl;

public class HiveBusinessImpl implements HiveBusiness {

    private HiveRepository hiveRepo;

    public HiveBusinessImpl(HiveRepository hiveRepo){
        this.hiveRepo = hiveRepo;
    }

    @Override
    public List<Hive> getHives(User user, int daysDelta) {
        // TODO implement daysDelta. Armandas
        List<Hive> subscribedHives = hiveRepo.getSubscribedHives(user);
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for(Hive hive: subscribedHives){
            // TODO implement timestamp. Armandas
            Hive h = hiveRepo.getHive(hive, new Timestamp(0), new Timestamp(1570195921501L+1000));
            hivesWithMeasurements.add(h);
        }
        return hivesWithMeasurements;
    }

    @Override
    public Hive getHiveMeasurements(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        return hiveRepo.getHive(hive, sinceTime, untilTime);
    }

    @Override
    public void subscribeHive(User user, Hive hive) {
        hiveRepo.subscribeHive(user, hive);
    }
}
