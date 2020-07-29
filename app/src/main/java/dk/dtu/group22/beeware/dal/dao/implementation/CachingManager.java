package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.Context;

import androidx.core.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

/**
 * This class checks whether a hive is cached within the given
 * since and until times in the request.
 * If the hive not exists in the List<Hive>, then check for the hive in the file system
 * If the hive still not found in the file system then create a new hive from HiveTool.
 * If the hive exists, but some measurements missing for given time,
 * so only the missing data fetches.
 * <p>
 * IMPORTANT:
 * Must be careful calling this function for a time period very
 * long time ago, because it will fetch all data between that period and
 * existing data in order to avoid gaps between data.
 * <p>
 * USER MANUAL:
 * In order to use file caching ctx should be set by using setCtx(Context ctx) method
 * To Turn OFF file caching, just do not set ctx.
 * To Turn OFF caching totally use HiveHiveTool class directly in the caller.
 * <p>
 * TESTS:
 * JUnit tests are written in LogicTest class. Tests must be run every time if
 * there are made any modifications in caching.
 */
public class CachingManager {
 //   private List<Hive> cachedHives;
    private WebScraper webScraper;
    private Context ctx;
    private final static CachingManager CACHING_MANAGER = new CachingManager();
    private CachedHiveRepoI repo = null;
   // private boolean isConnectionFailed = false;

    private CachingManager() {
    //    cachedHives = Collections.synchronizedList(new ArrayList<>());
        webScraper = new WebScraper();
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    private void initRepo() {
        if(repo == null){
          if(this.ctx != null){
              repo = new CachedHiveRepoSQLImpl(this.ctx);
          } else {
              // FIXME Throw exception
          }
        }
    }
/*
    public boolean isConnectionFailed() {
        return isConnectionFailed;
    }*/

    public static CachingManager getSingleton() {
        return CACHING_MANAGER;
    }

    public Hive getHive(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException, AccessLocalFileException {
       // Hive hive = findCachedHive(id);
        initRepo();
        Hive hive = repo.getCachedHiveWithAllData(id);
        if (hive != null) {
            updateHive(hive, sinceTime, untilTime);
        } else {
            hive = createHive(id, sinceTime, untilTime);
        }

        return hive;
    }
    // Commented, because it is redundant method. findCachedHive can be used instead
    //   public Hive getCachedHive(int id) {
//        for (Hive hive : cachedHives) {
//            if (id == hive.getId()) {
//                return hive;
//            }
//        }
//        return null;
//    }

    private void updateHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException, AccessLocalFileException {
        Timestamp sinceTimeDelta = new Timestamp(sinceTime.getTime() + 300000 * 2);
        Timestamp untilTimeDelta = new Timestamp(untilTime.getTime() - 300000 * 2);

        boolean isWithinSince = sinceTimeDelta.after(hive.getMeasurements().get(0).getTimestamp());
        boolean isWithinUntil = untilTimeDelta.before(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp());
        boolean isUpdated = false;
        if (!isWithinSince) {
            List<Measurement> list = fetchFromHiveTool(hive, sinceTime,
                    new Timestamp(hive.getMeasurements().get(0).getTimestamp().getTime()));
            if (list != null) {

                hive.getMeasurements().addAll(0, list);
                isUpdated = true;
            }
        }
        if (!isWithinUntil) {
            List<Measurement> list = fetchFromHiveTool(hive, hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp(), untilTime);
            if (list != null) {
                hive.getMeasurements().addAll(list);
                isUpdated = true;
            }
        }
        if (isUpdated && ctx != null) {
            trimMeasurements(hive); // Remove older measurements
        //    writeToFile(hive);
            initRepo();
            repo.updateHive(hive);
        }
    }

    private List<Measurement> fetchFromHiveTool(Hive hive, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException {
//        try {
            List<Measurement> mList = webScraper.getHiveMeasurements(hive.getId(), sinceTime, untilTime).first;
  //          isConnectionFailed = false;
            return mList;
 //       } catch (Exception e) { // Commented because The exception is handled further up.
 //           e.printStackTrace();
 //       }
 //       return null;
    }

    public Hive findCachedHive(int id) throws AccessLocalFileException {
    //    Hive foundHive;
    //    foundHive = retrieveHiveFromList(id);

        // If hive was not found in List<Hive> cachedHives, try to look in cached files
     //   if (foundHive == null && ctx != null) {
 //         Hive  foundHive = retrieveHiveFromFile(id);
     //   }

        initRepo();
        return repo.getCachedHiveWithAllData(id);
    }
/*
    private Hive retrieveHiveFromList(int id) {
        synchronized (cachedHives) {
            for (Hive hive : cachedHives) {
                if (hive.getId() == id) {
                    return hive;
                }
            }
        }
        return null;
    }*/

    private synchronized Hive retrieveHiveFromFile(int id) throws AccessLocalFileException {
        File file = new File(ctx.getCacheDir(), String.valueOf(id));
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
                ObjectInputStream is = new ObjectInputStream(bufferedInputStream);
                Hive hive = (Hive) is.readObject();
                is.close();
                bufferedInputStream.close();
                fis.close();
            //    cachedHives.add(hive);
                return hive;
            } catch (Exception e) {
                e.printStackTrace();
                throw new AccessLocalFileException(e.getMessage());
            }
        }
        return null;
    }

    private Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException, AccessLocalFileException {

        Pair<List<Measurement>, String> measurementsAndName = webScraper.getHiveMeasurements(id, sinceTime, untilTime);
        Hive hive = new Hive(id, measurementsAndName.second);
        hive.setMeasurements(measurementsAndName.first);
        if (ctx != null) {
            initRepo();
            repo.createCachedHive(hive);
         //   writeToFile(hive);
        }
      //  cachedHives.add(hive);

        return hive;
    }

    public synchronized void writeToFile(Hive hive) throws AccessLocalFileException {
        File file = new File(ctx.getCacheDir(), String.valueOf(hive.getId()));
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            ObjectOutputStream os = new ObjectOutputStream(bufferedOutputStream);
            os.writeObject(hive);
            os.close();
            bufferedOutputStream.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AccessLocalFileException(e.getMessage());
        }
    }

    /**
     * Clean out old dates automatically on the first and fifteenth of the month.
     * If more than 16 month are saved, the arraylist is shortened to 14 months.
     *
     * @param hive
     * A hive to check for old data
     * @pre The hive object has measurements
     * @post The hive object is guaranteed to have no older measurements than 16 months.
     */
    private void trimMeasurements(Hive hive) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.MONTH, -16);

        List<Measurement> measurements = hive.getMeasurements();
        Timestamp sixteenMonthsAgo = new Timestamp(cal.getTimeInMillis());

        if (measurements.get(0).getTimestamp().before(sixteenMonthsAgo)) {

            System.out.println("HiveObject: Cleaning up old data from hive " + hive.getId() + ", " + hive.getName() + ".");

            cal.add(Calendar.MONTH, 2);
            Timestamp fourteenMonthsAgo = new Timestamp(cal.getTimeInMillis());

            List<Measurement> temp = new ArrayList<Measurement>();
            for (Measurement mes : measurements){
                if (mes.getTimestamp().after(fourteenMonthsAgo)){
                    temp.add(mes);
                }
            }

            System.out.println("Oldest date: " + temp.get(0).getTimestamp().toString());
            hive.setMeasurements(temp);
        }
    }

    public void updateHive(Hive hive) {
        initRepo();
        repo.updateHive(hive);
    }

    /**
     * Should be used only for testing purposes
     */
    public void cleanCachedHives() {
        //cachedHives = new ArrayList<>();
    }
}
