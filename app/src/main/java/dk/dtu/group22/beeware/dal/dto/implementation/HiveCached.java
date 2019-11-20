package dk.dtu.group22.beeware.dal.dto.implementation;

import androidx.core.util.Pair;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;

public class HiveCached {

    private List<Hive> cachedHives;
    private HiveHivetool hiveHivetool;
    private final static HiveCached hiveCached = new HiveCached();

    public HiveCached(){
        cachedHives = new ArrayList<>();
        hiveHivetool = new HiveHivetool();
    }

    public void cleanCachedHives(){
        cachedHives = new ArrayList<>();
    }

    public static HiveCached getSingleton() {
        return hiveCached;
    }

    public Hive getHive(int id, Timestamp sinceTime, Timestamp untilTime){
        // TODO:
        // 0. Check if the hive is cached
        // 1. If cached return hive
        // 2. otherwise create hive
        // TODO:
        // How should additional measurements be added to the hive, now that the old ones are being deleted? How does it affect the graphs?
        Hive hive = findCachedHive(id);
        if (hive != null) {
            //boolean isWithinSince = hive.getMeasurements().get(0).getTimestamp().compareTo(sinceTime) >= 0;
            //boolean isWithinUntil = hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp().compareTo(sinceTime) <= 0;

            Timestamp sinceTimeDelta= new Timestamp(sinceTime.getTime() + 300000*2);
            Timestamp untilTimeDelta= new Timestamp(untilTime.getTime() - 300000*2);

            boolean isWithinSince = sinceTimeDelta.after(hive.getMeasurements().get(0).getTimestamp());
            boolean isWithinUntil = untilTimeDelta.before(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp());
            if(!isWithinSince){
                List<Measurement> list = hiveHivetool.getHiveMeasurements(id, sinceTime, new Timestamp(hive.getMeasurements().get(0).getTimestamp().getTime())).first;
                if (list != null) {
                    hive.getMeasurements().addAll(0,list);
                }
            }


            if(!isWithinUntil){
                List<Measurement> list = hiveHivetool.getHiveMeasurements(id, hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp(), untilTime).first;
                if (list != null) {
                    hive.getMeasurements().addAll(list);
                }
            }
         } else {
            hive = createHive(id, sinceTime, untilTime);
        }

        return hive;
    }

    private Hive findCachedHive(int id) {
        for (Hive hive : cachedHives) {
            if (hive.getId() == id) {
                return hive;
            }
        }
        return null;
    }

    private Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) {

        Pair<List<Measurement>, String> measurementsAndName = hiveHivetool.getHiveMeasurements(id, sinceTime, untilTime);

        Hive hive = new Hive(id, measurementsAndName.second);
        hive.setMeasurements(measurementsAndName.first);
        cachedHives.add(hive);

        return hive;
    }
}
