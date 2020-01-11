package dk.dtu.group22.beeware.business.implementation;

import android.content.Context;

import com.yariksoffice.lingver.Lingver;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;
import dk.dtu.group22.beeware.dal.dao.implementation.SubscriptionHivetool;
import dk.dtu.group22.beeware.dal.dao.implementation.SubscriptionManager;
import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscriptionManager;
import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

public class Logic {
   // private WebScraper hiveHivetool;
    private CachingManager cachingManager;
    private ISubscription subscriptionHivetool;
    private ISubscriptionManager subscriptionManager;
    private Context ctx;
    private final static Logic logic = new Logic();


    public static Logic getSingleton() {
        return logic;
    }

    public Logic() {
       // this.hiveHivetool = new WebScraper();
        this.cachingManager = CachingManager.getSingleton();
        this.subscriptionHivetool = new SubscriptionHivetool();


    }

    public void setContext(Context context) {
        this.ctx = context;
        this.subscriptionManager = new SubscriptionManager(context);
    }

    public void subscribeHive(int id) {
        subscriptionManager.saveSubscription(id);
    }

    public List<Integer> getSubscriptionIDs() {
        return subscriptionManager.getSubscriptions();
    }

    public void unsubscribeHive(int id) {
        subscriptionManager.deleteSubscription(id);
    }

    public List<Hive> getSubscribedHives(int daysDelta) {
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Integer> subscribedHives = this.getSubscriptionIDs();
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int id : subscribedHives) {
            Runnable runnable = () -> hivesWithMeasurements.add(getHive(id, new Timestamp(since), new Timestamp(now)));
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

    public Hive getHive(int id, Timestamp sinceTime, Timestamp untilTime) {

        Hive hive = cachingManager.getHive(id, sinceTime, untilTime);
        setCurrValues(hive);
        calculateHiveStatus(hive);

        return hive;
    }


    public List<NameIdPair> getNamesAndIDs() {
        try {
            List<NameIdPair> hivesIdName = subscriptionHivetool.getHivesToSubscribe();
            if (hivesIdName == null || hivesIdName.isEmpty()) {
                throw new HivesToSubscribeNoFound("Business error. Unable to fetch data");
            } else {
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
     * @param hive The hive to calculate statuses for
     */
    private void calculateHiveStatus(Hive hive) {
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


        // TODO: Make a manager for preferences, so that the configured value is not hardcoded

        // Use case: User has set a critical threshold for weight, which the hive must not fall below
        StatusCalculator weightFallsBelowConfiguredValue = (Hive inputHive) -> {
            double configuredWeightThreshold = 15.0;
            //System.out.println("Hive:" + inputHive.getName() + " Curr weight:" + inputHive.getCurrWeight());
            if (inputHive.getCurrWeight() < configuredWeightThreshold) {
                return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.DANGER, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            }
            return new Hive.StatusIntrospection(Hive.Variables.WEIGHT, Hive.Status.OK, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
        };
        calculators.add(weightFallsBelowConfiguredValue);


        // Use case: User has set a critical threshold for temp, which the hive must not fall below
        StatusCalculator tempFallsBelowConfiguredValue = (Hive inputHive) -> {
            double configuredTempThreshold = 30.0;
            if (inputHive.getCurrTemp() < configuredTempThreshold) {
                return new Hive.StatusIntrospection(Hive.Variables.TEMPERATURE, Hive.Status.WARNING,
                        Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
            }
            return new Hive.StatusIntrospection(Hive.Variables.TEMPERATURE, Hive.Status.OK, Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD);
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
            //if (tmp == null) {
            //    continue;
            //}
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
                // Do nothing
            }
        }
        hive.setStatusIntrospection(statusReasonings);
    }

    private Hive.Status worst(Hive.Status a, Hive.Status b) {
        if (a == Hive.Status.UNDEFINED) {
            return b;
        }
        if (b == Hive.Status.UNDEFINED) {
            return a;
        }

        if (a == Hive.Status.DANGER || b == Hive.Status.DANGER) {
            return Hive.Status.DANGER;
        }

        if (b == Hive.Status.WARNING || a == Hive.Status.WARNING) {
            return Hive.Status.WARNING;
        }

        return Hive.Status.OK;
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


    class HiveNotFound extends RuntimeException {
        public HiveNotFound(String msg) {
            super(msg);
        }
    }

    class HivesToSubscribeNoFound extends RuntimeException {
        public HivesToSubscribeNoFound(String msg) {
            super(msg);
        }
    }

    interface StatusCalculator {
        Hive.StatusIntrospection calculate(Hive hive);
    }

}
