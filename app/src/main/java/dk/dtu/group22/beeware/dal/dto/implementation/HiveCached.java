package dk.dtu.group22.beeware.dal.dto.implementation;

import androidx.core.util.Pair;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;

/**
 * This class checks whether a hive is cached within the given
 * since and until times in the request.
 * If the hive not exists in the cache, create a new hive from HiveTool.
 * If the hive exists, but some measurements missing for given time,
 * so only the missing data fetches.
 * Important: Must be careful calling this function for a time period very
 * long time ago, because it will fetch all data between that period and
 * existing data in order to avoid gaps between data.
 */
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
        Hive hive = findCachedHive(id);
        if (hive != null) {
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
