package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveSubscriptionRepository;

public class HiveSubscriptionsRepoArrayListImpl implements HiveSubscriptionRepository {
    private static List<Hive> subscribeHives = new ArrayList<>();

    static {
        // dummy subscriptions
        Hive hive = new Hive();
        hive.setName("Hive_a");
        hive.setId(101);
        Hive hive2 = new Hive();
        hive2.setName("Hive_c");
        hive2.setId(102);
        Hive hive3 = new Hive();
        hive3.setName("Hive_d");
        hive3.setId(103);
        Hive hive4 = new Hive();
        hive4.setName("Hive_e");
        hive4.setId(104);
        Hive hive5 = new Hive();
        hive5.setName("Hive_f");
        hive5.setId(105);
        Hive hive6 = new Hive();
        hive6.setName("Hive_g");
        hive6.setId(106);
        Hive hive7 = new Hive();
        hive7.setName("Hive_h");
        hive7.setId(107);
        Hive hive8 = new Hive();
        hive8.setName("Hive_i");
        hive8.setId(108);
        Hive hive9 = new Hive();
        hive9.setName("Hive_j");
        hive9.setId(109);
        Hive hive10 = new Hive();
        hive10.setName("Hive_k");
        hive10.setId(110);
        Hive hive11 = new Hive();
        hive11.setName("Hive_l");
        hive11.setId(111);
       subscribeHives.add(hive);
        subscribeHives.add(hive2);
        subscribeHives.add(hive3);
        subscribeHives.add(hive4);
        subscribeHives.add(hive5);
        subscribeHives.add(hive6);
        subscribeHives.add(hive7);
        subscribeHives.add(hive8);
        subscribeHives.add(hive9);
        subscribeHives.add(hive10);
        subscribeHives.add(hive11);


        //subscribeHives.add(104);
    }

    @Override
    public List<Hive> getHivesToSubscribe() {
        return subscribeHives;
    }
}
