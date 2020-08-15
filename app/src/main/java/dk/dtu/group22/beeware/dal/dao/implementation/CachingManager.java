package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.Context;

import androidx.core.util.Pair;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public class CachingManager {
    private WebScraper webScraper;
    private Context ctx;
    private final static CachingManager CACHING_MANAGER = new CachingManager();
    private CachedHiveRepoI repo = null;

    private CachingManager() {
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
              throw new IllegalArgumentException("Context is null");
          }
        }
    }

    public static CachingManager getSingleton() {
        return CACHING_MANAGER;
    }

    public Hive getCachedHiveAndUpdateOrCreateUsesNetwork(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException
            {
        initRepo();
        Hive hive = repo.getHiveWithinPeriod(id, sinceTime, untilTime);
        if (hive != null) {
            updateHive(hive, sinceTime, untilTime);
        } else {
            hive = createHive(id, sinceTime, untilTime);
        }

        return hive;
    }

    private void updateHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException//, AccessLocalFileException
     {
        initRepo();
        Timestamp sinceTimeDelta = new Timestamp(sinceTime.getTime() + 300000 * 2);
        Timestamp untilTimeDelta = new Timestamp(untilTime.getTime() - 300000 * 2);

        boolean isWithinSince = sinceTimeDelta.after(hive.getMeasurements().get(0).getTimestamp());
        boolean isWithinUntil = untilTimeDelta.before(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp());
        if (!isWithinSince) {
            List<Measurement> list =  webScraper.getHiveMeasurements(hive.getId(), sinceTime,
                    new Timestamp(hive.getMeasurements().get(0).getTimestamp().getTime())).first;
            if (list != null) {
                hive.getMeasurements().addAll(0, list);
                repo.saveNewMeasurements(hive, list);
            }
        }
        if (!isWithinUntil) {
            List<Measurement> list =  webScraper.getHiveMeasurements(hive.getId(), hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp(), untilTime).first;
            if (list != null) {
                hive.getMeasurements().addAll(list);
                repo.saveNewMeasurements(hive, list);
            }
        }
    }

    public Hive getCachedHive(int id)
     {
        initRepo();
        return repo.getCachedHiveWithAllData(id);
    }
    public Hive getCachedHiveWithinPeriod(int id, Timestamp since, Timestamp until) {
        initRepo();
        return repo.getHiveWithinPeriod(id, since, until);
    }

    private Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException//, AccessLocalFileException
     {

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

    public void updateHiveMetaData(Hive hive) {
        initRepo();
        repo.updateHiveMetaData(hive);
    }

    public void downloadOldDataInBackground(int id) throws IOException, NoDataAvailableOnHivetoolException//, AccessLocalFileException
     {

         initRepo();
        List<Measurement> minMaxMeasurements = repo.fetchMinMaxMeasurementsByTimestamp(id);
        Timestamp endDate = minMaxMeasurements.get(0).getTimestamp();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endDate.getTime());
        cal.add(Calendar.DATE, -7);
        Timestamp startDate = new Timestamp(cal.getTimeInMillis());
        startDate = roundDateToMidnight(startDate);
        Timestamp a = new Timestamp(startDate.getTime());
        Timestamp b = new Timestamp(endDate.getTime());

        while(true){
            try {
                System.out.println("downloadOldDataInBackground: Downloaded Hive " + id + ", " +
                        "from " + a.toString().substring(0, 10) + " " +
                        "to " + b.toString().substring(0, 10) + ".");

                a = new Timestamp(startDate.getTime());
                b = new Timestamp(endDate.getTime());
                getCachedHiveAndUpdateOrCreateUsesNetwork(id, a, b);
                endDate = new Timestamp(startDate.getTime());
                cal.setTimeInMillis(endDate.getTime());
                cal.add(Calendar.DATE, -7); // substract 7 days
                startDate = new Timestamp(cal.getTimeInMillis());
            }
            catch (NoDataAvailableOnHivetoolException e){
                System.out.printf("downloadHiveData: No data available on hivetool to download hive data for hive " +
                        id + " from" + a + " to " + b + ".");
                e.printStackTrace();
                throw new NoDataAvailableOnHivetoolException(e.getMessage());
            }
            catch (IOException e) {
                System.out.println("downloadOldDataInBackground: FAILED to download Hive " + id + ", " +
                        "from " + a.toString().substring(0, 10) + " " +
                        "to " + b.toString().substring(0, 10) + ".");
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
        }
    }

    /**
     * Resets the clock on a Timestamp
     * @param stamp
     * A TimeStamp to set to time 00:00
     * @return A TimeStamp with time set to 00:00
     */
    private Timestamp roundDateToMidnight(Timestamp stamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(stamp.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp result = new Timestamp(cal.getTimeInMillis());
        System.out.println("roundDateToMidnight: Corrected the date to midnight: " + result);
        return result;
    }

    public Hive getHiveWithMostRecentData(int id, long timeDelta) {
        initRepo();
        return repo.getHiveWithMostRecentData(id, timeDelta);
    }

    public Hive fetchHiveMetaData(int hiveId){
        return repo.fetchHiveMetaData(hiveId);
    }

    public List<Measurement> fetchMinMaxMeasurementsByTimestamp(int hiveId) {
        return repo.fetchMinMaxMeasurementsByTimestamp(hiveId);
    }
}
