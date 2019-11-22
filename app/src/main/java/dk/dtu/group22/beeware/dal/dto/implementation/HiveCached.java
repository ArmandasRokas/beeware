package dk.dtu.group22.beeware.dal.dto.implementation;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscription;

/**
 * This class checks whether a hive is cached within the given
 * since and until times in the request.
 * If the hive not exists in the List<Hive>, then check for the hive in the file system
 * If the hive still not found in the file system then create a new hive from HiveTool.
 * If the hive exists, but some measurements missing for given time,
 * so only the missing data fetches.
 * Important: Must be careful calling this function for a time period very
 * long time ago, because it will fetch all data between that period and
 * existing data in order to avoid gaps between data.
 */
public class HiveCached {

    private List<Hive> cachedHives;
    private HiveHivetool hiveHivetool;
    private Context ctx;
    private final static HiveCached hiveCached = new HiveCached();
    private boolean isConnectionFailed = false;

    private HiveCached(){
        cachedHives = new ArrayList<>();
        hiveHivetool = new HiveHivetool();
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public boolean isConnectionFailed() {
        return isConnectionFailed;
    }


    public static HiveCached getSingleton() {
        return hiveCached;
    }

    public Hive getHive(int id, Timestamp sinceTime, Timestamp untilTime){
        Hive hive = findCachedHive(id);
        if (hive != null) {
            updateHive(hive, sinceTime, untilTime);
        } else {
            hive = createHive(id, sinceTime, untilTime);
        }
        return hive;
    }

    private void updateHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        Timestamp sinceTimeDelta= new Timestamp(sinceTime.getTime() + 300000*2);
        Timestamp untilTimeDelta= new Timestamp(untilTime.getTime() - 300000*2);

        boolean isWithinSince = sinceTimeDelta.after(hive.getMeasurements().get(0).getTimestamp());
        boolean isWithinUntil = untilTimeDelta.before(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp());
        boolean isUpdated = false;
        if(!isWithinSince){
            List<Measurement> list = fetchFromHiveTool(hive, sinceTime, new Timestamp(hive.getMeasurements().get(0).getTimestamp().getTime()) );
            if (list != null) {
                hive.getMeasurements().addAll(0, list);
                isUpdated = true;
            }
        }
        if(!isWithinUntil){
            List<Measurement> list = fetchFromHiveTool(hive, hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp(), untilTime);
            if (list != null) {
                hive.getMeasurements().addAll(list);
                isUpdated = true;
            }
        }
        if(isUpdated && ctx != null){
            writeToFile(hive);
        }
    }

    private List<Measurement> fetchFromHiveTool(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        try {
            List<Measurement> mList = hiveHivetool.getHiveMeasurements(hive.getId(), sinceTime, untilTime).first;
            isConnectionFailed = false;
            return mList;
        } catch (ISubscription.UnableToFetchData e){
            isConnectionFailed = true;
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Hive findCachedHive(int id) {
        Hive foundHive;
        foundHive = retrieveHiveFromList(id);

        // If hive was not found in List<Hive> cachedHives, try to look in cached files
        if(foundHive == null && ctx != null){
            foundHive = retrieveHiveFromFile(id);
        }
        return foundHive;
    }

    private Hive retrieveHiveFromList(int id) {
        for (Hive hive : cachedHives) {
            if (hive.getId() == id) {
                return hive;
            }
        }
        return null;
    }

    private Hive retrieveHiveFromFile(int id){
        File file = new File(ctx.getCacheDir(), String.valueOf(id));
        if(file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                Hive hive = (Hive) is.readObject();
                is.close();
                fis.close();
                cachedHives.add(hive);
                return hive;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) {

        Pair<List<Measurement>, String> measurementsAndName = hiveHivetool.getHiveMeasurements(id, sinceTime, untilTime);
        Hive hive = new Hive(id, measurementsAndName.second);
        hive.setMeasurements(measurementsAndName.first);
        if(ctx != null){
            writeToFile(hive);
        }
        cachedHives.add(hive);

        return hive;
    }

    private void writeToFile(Hive hive) {
        File file = new File(ctx.getCacheDir(), String.valueOf(hive.getId()));
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(hive);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Should be used only for testing purposes
     */
    public void cleanCachedHives(){
        cachedHives = new ArrayList<>();
    }
}
