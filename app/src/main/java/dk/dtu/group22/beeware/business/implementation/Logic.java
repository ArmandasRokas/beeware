package dk.dtu.group22.beeware.business.implementation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;
import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;
import dk.dtu.group22.beeware.dal.dao.implementation.NoDataAvailableOnHivetoolException;
//import dk.dtu.group22.beeware.dal.dao.implementation.AccessLocalFileException;
import dk.dtu.group22.beeware.dal.dao.implementation.SubscriptionHivetool;
import dk.dtu.group22.beeware.dal.dao.implementation.SubscriptionManager;
import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscriptionManager;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;
import dk.dtu.group22.beeware.presentation.Overview;

/**
 * The logic class, which is intended to do the bulk of the work in regards to managing and caching
 * hive data, and interacting with the HiveTool database.
 */
public class Logic {
    private CachingManager cachingManager;
    private ISubscription subscriptionHivetool;
    private ISubscriptionManager subscriptionManager;
    private Context ctx;
    private final static Logic logic = new Logic();
    private List<String> notFetchedHivesFromNetwork = new LinkedList<>();
    private List<String> notFetchedCachedHives = new LinkedList<>();
    private boolean backgroundDownloadInProgress = false;


    /**
     * @pre logic must be constructed
     * @post does the return
     * @inv Does not modify anything
     * @return The logic singleton stored in the class itself
     */
    public static Logic getSingleton() {
        return logic;
    }

    /**
     * Logic constructor which initializes the subsystems used to managing cache and subscriptions.
     */
    private Logic() {
        this.cachingManager = CachingManager.getSingleton();
        this.subscriptionHivetool = new SubscriptionHivetool();
    }

    /**
     * Call context in order to access and modify preferenceManager
     * @param context
     * An Android Framework context
     */
    public void setContext(Context context) {
        this.ctx = context;
        this.subscriptionManager = new SubscriptionManager(context);
    }

    /**
     * notFetchedHives, getNotFetchedHives() and clearNotFetchHives()
     * is a work around for a error handling (a feedback to user if
     * there was a problem downloading hives). An exception could not
     * be thrown in getSubscribedHives, because it crashes the app.
     */
    public List<String> getNotFetchedHivesFromNetwork() {
        return notFetchedHivesFromNetwork;
    }

    public List<String> getNotFetchedCachedHives() {
        return notFetchedCachedHives;
    }

    public void clearNotFetchHivesFromNetwork(){
        notFetchedHivesFromNetwork = new LinkedList<>();
    }
    public void clearNotFetchHivesFromFile(){
        notFetchedCachedHives = new LinkedList<>();
    }
    /**
     * Subscribes a hive
     * @param id
     * An id of a specific hive on HiveTools
     * @pre context of the application is given by setContext, where sharedPreferences
     * is set up according to the needs of the subscriptionManager (look there)
     * @post Saves the id persistently in the application
     * @inv It only modifies sharedPreferences
     */
    public void subscribeHive(int id) {
        subscriptionManager.saveSubscription(id);
    }

    /**
     * Returns the hives that have been subscribed to
     * @pre context of the application is given by setContext, where sharedPreferences
     * is set up according to the needs of the subscriptionManager (look there)
     * @post Returns a list
     * @inv Does not modify any data
     * @return a list of ids, of each hive subscribed to.
     */
    public List<Integer> getSubscriptionIDs() {
        return subscriptionManager.getSubscriptions();
    }

    /**
     * Removes a hive that was previously subscribed to
     * @param id
     * An id of a hive that was, before the call, subscribed to.
     * @pre the id is among of the hives previously subscribed to. Context of the application is given by setContext, where sharedPreferences
     *       is set up according to the needs of the subscriptionManager (look there)
     * @post the list of hives have been modified to not contain 'id'
     * @inv it only modifies sharedPreferences, see subscriptionManager (look there)
     */
    public void unsubscribeHive(int id) {
        subscriptionManager.deleteSubscription(id);
    }

    /**
     * Get hives that have been subscribed to, and guarantee that we have the most current data for daysDelta number of days
     * @param daysDelta
     * The number of days we want to have data for. In short we will have data for 24 hours times daysDelta
     *
     * @pre setContext have been called.
     * @post All hives are guaranteed to have the number of days of data.
     * @inv Can only risk modifying hives, if the number of days of data is not present.
     * @return
     * A list of hives that have been subscribed to.
     */
    public List<Hive> getSubscribedHives(int daysDelta) {
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Integer> subscribedHives = this.getSubscriptionIDs();
        List<Hive> hivesWithMeasurements = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        for (int id : subscribedHives) {
            Runnable runnable = () -> {
                Hive h;
                try {
                    h = getHiveNetworkAndSetCurrValues(id, new Timestamp(since), new Timestamp(now));
                    if(h == null){
                        throw new Exception("Hive: " + id + " is null.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    notFetchedHivesFromNetwork.add(subscriptionManager.getCachedNameIdPair(id).getName());
                    h = getHiveWithMostRecentDataAndSetCurrValues(id, 86400000 * daysDelta);
                }
                if(h == null){
                    // If hive is still null it was not possible to fetch a hive either from network or cahced.
                    // In this case create empty hive to visualize that there was something wrong in getting a hive.
                    h = Hive.createEmptyHive(id,subscriptionManager.getCachedNameIdPair(id).getName(), 86400000 * daysDelta);
                    notFetchedCachedHives.add(subscriptionManager.getCachedNameIdPair(id).getName());
                }
                hivesWithMeasurements.add(h);

            };
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return hivesWithMeasurements;
    }

    private Hive getHiveWithMostRecentDataAndSetCurrValues(int id, long timeDelta) {
        Hive hive = cachingManager.getHiveWithMostRecentData(id, timeDelta);
        if(hive != null){
            setCurrValues(hive);
            return hive;
        } else{
            return null;
        }
    }

    /**
     * Returns a hive of a specific ID. The hive is guaranteed to have data from sinceTime till untilTime
     * @param id
     * The id of a specific hive
     * @param sinceTime
     * @param untilTime
     * @pre the id must exist in the hivetool database.
     * @post All hives are guaranteed to have the number of days of data.
     * @inv Can only risk modifying hives, if the number of days of data is not present.
     * @return
     * A hive, with data specified by the parameters.
     */
    public Hive getHiveNetworkAndSetCurrValues(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException
            //, AccessLocalFileException
        {

        Hive hive = cachingManager.getCachedHiveAndUpdateOrCreateUsesNetwork(id, sinceTime, untilTime);
        setCurrValues(hive);

        return hive;
    }

    /**
     *
     * @param id
     * The identifier of a specific hive in the database.
     * @pre The hive with identifier 'id' must have been cached.
     * @post Returns a cached hive.
     * @inv It does not modify any data
     * @return Hive with the identifier id, if it has been cached. Otherwise returns null.
     * The time complexity is linear with the number of hives that have been cached.
     */
    public Hive getCachedHive(int id)// throws AccessLocalFileException
     {
        Hive hive = cachingManager.getCachedHive(id);
        if(hive != null){
            setCurrValues(hive);
            return hive;
        } else{
            return null;
        }
    }

    public List<NameIdPair> getNamesAndIDs() {
        try {
            List<NameIdPair> hivesIdName = subscriptionHivetool.getHivesToSubscribe();
            if (hivesIdName == null || hivesIdName.isEmpty()) {
                throw new HivesToSubscribeNoFound("Business error. Unable to fetch data");
            } else {
                subscriptionManager.cacheHivesToSub(hivesIdName);
                return hivesIdName;
            }
        } catch (Exception e) {
            throw new HivesToSubscribeNoFound(e.getMessage());
        }
    }

    /**
     * Calculates status for the hive, and stores all the reasons in the hives statusIntrospection.
     * It sets the status of each variable to the worst it could find according to the different use-cases.
     * One lambda is one use-case, which affect the status in some way.
     *
     * @param hive The hive to calculate statuses for.
     *             Pre-condition: Hive must contain newest data
     *             Post-condition: Hive will now have a StatusIntrospection object,
     *             denoting its statuses, it will also calculate
     *             delta value of weight between the previous two midnights.
     *             The hive will have its status set, for each variable,
     *             set to the "most dangerous" level.
     *             That means that if it has two statuses for Weight, but one is of  level Ok,
     *             and another is of level Danger, then it will set the weight
     *             status of the hive to Danger
     */
    /**
     * Calculates various statuses on the hive and stores it in the hive parameter
     * @pre Hive has at least two days of data
     * @post Hive will be modified to have various statuses calculated. Statuses will remain
     * undefined if hive is less than two days old.
      * @param hive
     */
    public void calculateHiveStatus(Hive hive) {
        hive.resetStatuses();
        // Calculate various enum statuses
        List<StatusCalculator> calculators = new ArrayList<>();

        // Use case: Calculate specific delta to be displayed by a hive.
        StatusCalculator calculateDelta = (Hive inputHive) -> {
            // Calculate Hive delta
            Calendar today = Calendar.getInstance();

            long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTimeInMillis(today.getTimeInMillis() - twentyFourHoursInMillis);

            int prevMidnightIndex = getClosestMidnightValIndex(inputHive, today, 0);
            int prevprevMidnightIndex = getClosestMidnightValIndex(inputHive, yesterday, inputHive.getMeasurements().size() - prevMidnightIndex - 1);
            Timestamp prevTime = inputHive.getMeasurements().get(prevMidnightIndex).getTimestamp();
            Timestamp prevprevTime = inputHive.getMeasurements().get(prevprevMidnightIndex).getTimestamp();

            if (!(isAroundMidnight(prevTime, today) && isAroundMidnight(prevprevTime, yesterday))) {
                inputHive.setWeightDelta(Double.NaN);
                return new Hive.StatusIntrospection(Hive.Variables.OTHER, Hive.Status.UNDEFINED, Hive.DataAnalysis.CASE_DELTA_CALCULATIONS);
            } else {
                double prevMidnightWeight = inputHive.getMeasurements().get(prevMidnightIndex).getWeight();
                double prevprevMidnightWeight = inputHive.getMeasurements().get(prevprevMidnightIndex).getWeight();
                double deltaWeight = prevMidnightWeight - prevprevMidnightWeight;
                inputHive.setWeightDelta(deltaWeight);
                return new Hive.StatusIntrospection(Hive.Variables.OTHER, Hive.Status.OK, Hive.DataAnalysis.CASE_DELTA_CALCULATIONS);
            }
        };
        calculators.add(calculateDelta);


        // Use case: User has set a critical threshold for weight, which the hive must not fall below
        StatusCalculator weightFallsBelowConfiguredValue = (Hive inputHive) -> {
            double configuredWeightThreshold = inputHive.getWeightIndicator();
            if (inputHive.getCurrWeight() > configuredWeightThreshold) {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.OK, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            } else if (inputHive.getCurrWeight() < configuredWeightThreshold - 5) {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.DANGER, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            } else {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.WARNING, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            }

        };
        calculators.add(weightFallsBelowConfiguredValue);


        // Use case: User has set a critical threshold for temp, which the hive must not fall below
        StatusCalculator tempFallsBelowConfiguredValue = (Hive inputHive) -> {
            double configuredTempThreshold = inputHive.getTempIndicator();
            if (inputHive.getCurrTemp() > configuredTempThreshold) {
                return new Hive.StatusIntrospection(Hive.Variables.TEMPERATURE, Hive.Status.OK, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            } else if (inputHive.getCurrTemp() < configuredTempThreshold - 10) {
                return new Hive.StatusIntrospection(Hive.Variables.TEMPERATURE, Hive.Status.DANGER, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            } else {
                return new Hive.StatusIntrospection(Hive.Variables.TEMPERATURE, Hive.Status.WARNING, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            }
        };
        calculators.add(tempFallsBelowConfiguredValue);


        // Use case: User has set a maximum rate of change (negative slope) for the weight, which the hive must not exceed.
        StatusCalculator suddenChangeInWeight = (Hive inputHive) -> {

            // 2 kg
            double configuredWeightDelta = -2.0;
            // 10 minutes. In millis
            double configuredTimeDelta = 10.0 * 60 * 1000;

            // Hardcoded rate of change: -2kg / 10 minutes
            double configuredRateOfChange = configuredWeightDelta / configuredTimeDelta;

            // Check for 1 week. In millis
            double configuredTimeInterval = 60 * 60 * 24 * 7 * 1000;

            final int n = inputHive.getMeasurements().size();

            final List<Measurement> data = inputHive.getMeasurements();

            int startIndex = -1;
            int endIndex = -1;
            long currentEndTime = data.get(n - 1).getTimestamp().getTime();
            for (int i = 0; data.get(n - 1).getTimestamp().getTime() - configuredTimeDelta > currentEndTime; ) {
                currentEndTime = data.get(n - i - 1).getTimestamp().getTime();
                int currentEndIndex = n - i - 1;
                int currentStartIndex = n - i - 1;
                long currentStartTime = currentEndTime;
                while (currentStartTime + configuredTimeDelta > currentEndTime) {
                    currentStartTime = data.get(n - i - 1).getTimestamp().getTime();
                    i++;
                }
                currentStartIndex = n - i - 1;

                // Check if it is around ten minutes.
                long fiveMinutes = 5 * 60 * 60 * 1000;
                // Is fine if the time intervals is around
                boolean isAroundTenMinutes = configuredTimeDelta <= currentEndTime - currentStartTime && currentEndTime - currentStartTime <= configuredTimeDelta + fiveMinutes;
                if (!isAroundTenMinutes) {
                    continue;
                }
                double deltaWeight = data.get(currentEndIndex).getWeight() - data.get(currentStartIndex).getWeight();
                double deltaTime = data.get(currentEndIndex).getTimestamp().getTime() - data.get(currentStartIndex).getTimestamp().getTime();
                double currentRateOfChange = deltaWeight / deltaTime;
                if (currentRateOfChange <= configuredRateOfChange) {
                    startIndex = currentStartIndex;
                    endIndex = currentEndIndex;
                    break;
                }
            }

            if (startIndex == -1 || endIndex == -1) {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.OK, Hive.DataAnalysis.CASE_SUDDEN_CHANGE);
            }
            // Check if it is between may and august
            Calendar today = Calendar.getInstance();
            int day = 1;
            int may_month = Calendar.MAY;
            int august_month = Calendar.AUGUST;
            int year = today.get(Calendar.YEAR);
            int midnight_hour = 0;
            int midnight_minutes = 0;
            Calendar firstOfMay = Calendar.getInstance();
            firstOfMay.set(year, may_month, day, midnight_hour, midnight_minutes);
            Calendar firstOfAugust = Calendar.getInstance();
            firstOfAugust.set(year, august_month, day, midnight_hour, midnight_minutes);
            long timeFound = data.get(startIndex).getTimestamp().getTime();
            if (firstOfMay.getTimeInMillis() <= timeFound && timeFound <= firstOfAugust.getTimeInMillis()) {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.WARNING, Hive.DataAnalysis.CASE_SUDDEN_CHANGE);
            } else {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.DANGER, Hive.DataAnalysis.CASE_SUDDEN_CHANGE);
            }

        };
        calculators.add(suddenChangeInWeight);

        List<Hive.StatusIntrospection> statusReasonings = new ArrayList<>();
        for (StatusCalculator calculator : calculators) {
            Hive.StatusIntrospection tmp = calculator.calculate(hive);
            statusReasonings.add(tmp);

            if (tmp.getVariable() == Hive.Variables.HUMIDITY) {
                Hive.Status worst = worst(tmp.getStatus(), hive.getHumidStatus());
                hive.setHumidStatus(worst);
            } else if (tmp.getVariable() == Hive.Variables.ILLUMINANCE) {
                Hive.Status worst = worst(tmp.getStatus(), hive.getIllumStatus());
                hive.setIllumStatus(worst);
            } else if (tmp.getVariable() == Hive.Variables.WEIGHT) {
                Hive.Status worst = worst(tmp.getStatus(), hive.getWeightStatus());
                hive.setWeightStatus(worst);
            } else if (tmp.getVariable() == Hive.Variables.TEMPERATURE) {
                Hive.Status worst = worst(tmp.getStatus(), hive.getTempStatus());
                hive.setTempStatus(worst);
            } else if (tmp.getVariable() == Hive.Variables.OTHER) {
                if(tmp.getReasoning() == Hive.DataAnalysis.CASE_DELTA_CALCULATIONS && tmp.getStatus() == Hive.Status.UNDEFINED){
                    hive.setTempStatus(Hive.Status.UNDEFINED);
                    hive.setWeightStatus(Hive.Status.UNDEFINED);
                    hive.setHumidStatus(Hive.Status.UNDEFINED);
                    hive.setIllumStatus(Hive.Status.UNDEFINED);
                }
            }
        }
        hive.setStatusIntrospection(statusReasonings);
    }

    private Hive.Status worst(Hive.Status a, Hive.Status b) {

        if (a == Hive.Status.OK) {
            return b;
        }

        if (b == Hive.Status.OK) {
            return a;
        }

        if (a == Hive.Status.UNDEFINED || b == Hive.Status.UNDEFINED) {
            return Hive.Status.UNDEFINED;
        }

        if (a == Hive.Status.DANGER || b == Hive.Status.DANGER) {
            return Hive.Status.DANGER;
        }

        return Hive.Status.WARNING;
    }

    private void setCurrValues(Hive hive) {
        hive.setCurrWeight(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getWeight());
        hive.setCurrTemp(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTempIn());
        hive.setCurrIlluminance(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getIlluminance());
        hive.setCurrHum(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getHumidity());
    }

    private boolean isAroundMidnight(Timestamp time, Calendar day) {
        Calendar idealMidnight = getMidnightInstanceOfDay(day);
        int hourInMillis = 60 * 60 * 1000;
        long beforeMidnight = idealMidnight.getTimeInMillis() - hourInMillis;
        long afterMidnight = idealMidnight.getTimeInMillis() + hourInMillis;
        if (beforeMidnight <= time.getTime() && time.getTime() <= afterMidnight) {
            return true;
        }
        return false;
    }

    /**
     * @param hive
     * @param day        The day we want to find the (most recently occurring) midnight for. More specifically if you had day x at clock 22:10 it will find the index corresponding to day x at 00:00
     * @param startIndex Where to start to search in the data, from the end of the Hive.measurements list. Distance from end of list. 0 is equivalent to end of list.
     * @return index in the hive.measurments list, where the timestamp is the closest to the most recently occuring midnight, before the day variable.
     */
    private int getClosestMidnightValIndex(Hive hive, Calendar day, int startIndex) {
        Calendar idealPrevMidnight = getMidnightInstanceOfDay(day);
        int res = hive.getMeasurements().size() - 1 - startIndex;
        Timestamp closest_midnight_value = hive.getMeasurements().get(res).getTimestamp();
        Long smallestDeltaFromMidnight = Math.abs(idealPrevMidnight.getTimeInMillis() - closest_midnight_value.getTime());
        for (int i = startIndex; i < hive.getMeasurements().size(); ++i) {
            int currentIndex = hive.getMeasurements().size() - 1 - i;
            Timestamp midnight_candidate = hive.getMeasurements().get(currentIndex).getTimestamp();
            long currentDelta = Math.abs(midnight_candidate.getTime() - idealPrevMidnight.getTimeInMillis());
            if (currentDelta <= smallestDeltaFromMidnight) {
                smallestDeltaFromMidnight = currentDelta;
                res = currentIndex;
            } else {
                break;
            }
        }
        return res;
    }


    /**
     * @param date
     * @return A Calendar instance, where its parameters is exactly the same as date, but the hours and minutes are 0.
     */
    private Calendar getMidnightInstanceOfDay(Calendar date) {
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);
        int year = date.get(Calendar.YEAR);
        int midnight_hour = 0;
        int midnight_minutes = 0;
        Calendar midnight = Calendar.getInstance();
        midnight.set(year, month, day, midnight_hour, midnight_minutes);
        return midnight;
    }

    public void updateHiveMetaData(Hive hive) {
        cachingManager.updateHiveMetaData(hive);
    }

    public Hive getCachedHiveWithinPeriod(int hiveId, Timestamp from, Timestamp to) {
        return cachingManager.getCachedHiveWithinPeriod(hiveId, from, to);
    }

    public void downloadOldDataInBackground(int id)throws IOException
    {
        // check if download data is finished
        Hive hive = cachingManager.fetchHiveMetaData(id);
        if(!hive.isCachingFinished()){
            try {
                backgroundDownloadInProgress = true;
                cachingManager.downloadOldDataInBackground(id);
                backgroundDownloadInProgress = false;
            } catch (NoDataAvailableOnHivetoolException e){
                // When NoDataAvailable is thrown, so it means that no more data is on HiveTool
                // and caching is done.
                backgroundDownloadInProgress = false;
                hive.setCachingFinished(true);
                cachingManager.updateHiveMetaData(hive);
            }  catch (IOException e) {
                backgroundDownloadInProgress = false;
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
        }
    }

    public List<Measurement> fetchMinMaxMeasurementsByTimestamp(int hiveId) {
        return cachingManager.fetchMinMaxMeasurementsByTimestamp(hiveId);
    }

    public boolean isBackgroundDownloadInProgress() {
        return backgroundDownloadInProgress;
    }

    public void setBackgroundDownloadInProgress(boolean inProgress) {
        this.backgroundDownloadInProgress = inProgress;
    }

    class HivesToSubscribeNoFound extends RuntimeException {
        public HivesToSubscribeNoFound(String msg) {
            super(msg);
        }
    }

    interface StatusCalculator {
        Hive.StatusIntrospection calculate(Hive hive);
    }

    /**
     * Method to generate a notification for the user.
     *
     * @param details The info the notification should provide.
     */
    public void createNotification(String details) {

        PendingIntent pending = PendingIntent.getActivity(ctx, 1, new Intent(ctx, Overview.class), 0);

        NotificationCompat.Builder notificationBuilder;

        notificationBuilder = new NotificationCompat.Builder(ctx, "Beeware");
        notificationBuilder.setSmallIcon(R.drawable.img_beehive);
        notificationBuilder.setContentTitle("BEEWARE");
        notificationBuilder.setContentText("Dangers detected");
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(details));
        notificationBuilder.setAutoCancel(true);

        notificationBuilder.setContentIntent(pending);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(notificationManager);

        notificationManager.notify(1, notificationBuilder.build());
    }

    /**
     * Method to set up a channel for the notifications.
     */
    private void createNotificationChannel(NotificationManager not) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Beeware";
            String description = "Notifications from Beeware";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("Beeware", name, importance);
            channel.setDescription(description);

            not.createNotificationChannel(channel);
        }
    }

    public NameIdPair getCachedNameIdPair(int hiveId){
        return subscriptionManager.getCachedNameIdPair(hiveId);
    }
}
