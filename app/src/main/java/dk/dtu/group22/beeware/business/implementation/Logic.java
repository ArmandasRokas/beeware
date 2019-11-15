package dk.dtu.group22.beeware.business.implementation;

import android.content.Context;

import androidx.core.util.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.User;
import dk.dtu.group22.beeware.dal.dto.implementation.SubscriptionManager;
import dk.dtu.group22.beeware.dal.dto.interfaces.IHive;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscriptionManager;
import dk.dtu.group22.beeware.dal.dto.interfaces.IUser;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dto.implementation.HiveHivetool;
import dk.dtu.group22.beeware.dal.dto.implementation.SubscriptionHivetool;
import dk.dtu.group22.beeware.dal.dto.implementation.UserHiveIds;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dto.interfaces.IUser;
import dk.dtu.group22.beeware.dal.dto.interfaces.NameIdPair;

public class Logic {

    private HiveHivetool hiveHivetool;
    private ISubscription subscriptionHivetool;
    private ISubscriptionManager subscriptionManager;
    private Context ctx;
    private final static Logic logic = new Logic();
    private List<Hive> cachedHives;

    public static Logic getSingleton() {
        return logic;
    }


    public Logic() {
        this.hiveHivetool = new HiveHivetool();
        this.subscriptionHivetool = new SubscriptionHivetool();
        cachedHives = new ArrayList<>();
    }

    public void setContext(Context context) {
        this.ctx = context;
        this.subscriptionManager = new SubscriptionManager(context);
    }

    public List<Hive> getSubscribedHives(int daysDelta) {
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Integer> subscribedHives = this.getSubscriptionIDs();
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for (int id : subscribedHives) {
            hivesWithMeasurements.add(getHive(id, new Timestamp(since), new Timestamp(now)));
        }
        return hivesWithMeasurements;
    }

    public Hive getHive(int id, Timestamp sinceTime, Timestamp untilTime) {
        // TODO:
        // 0. Check if the hive is cached
        // 1. If cached return hive
        // 2. otherwise create hive
        // TODO:
        // How should additional measurements be added to the hive, now that the old ones are being deleted? How does it affect the graphs?
        Hive hive = findCachedHive(id);
        if (hive != null) {
            boolean isWithinSince = hive.getMeasurements().get(0).getTimestamp().compareTo(sinceTime) >= 0;
            boolean isWithinUntil = hive.getMeasurements().get(hive.getMeasurements().size() - 1).getTimestamp().compareTo(sinceTime) <= 0;
            if (!(isWithinSince && isWithinUntil)) {
                List<Measurement> list = hiveHivetool.getHiveMeasurements(id, sinceTime, untilTime).first;
                hive.setMeasurements(list);
            }
        } else {
            hive = createHive(id, sinceTime, untilTime);
        }

        calculateHiveStatus(hive);
        setCurrValues(hive);

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

    public List<Hive> getHives(int daysDelta) {
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Integer> subscribedHives = this.getSubscriptionIDs();
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for (int id : subscribedHives) {
            hivesWithMeasurements.add(getHive(id, new Timestamp(since), new Timestamp(now)));
        }
        return null;
    }


    public Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) {

        Pair<List<Measurement>, String> measurementsAndName = hiveHivetool.getHiveMeasurements(id, sinceTime, untilTime);

        Hive hive = new Hive(id, measurementsAndName.second);
        hive.setMeasurements(measurementsAndName.first);

        calculateHiveStatus(hive);
        setCurrValues(hive);

        return hive;
    }


    private void calculateHiveStatus(Hive hive) {
        Calendar today = Calendar.getInstance();

        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(today.getTimeInMillis() - twentyFourHoursInMillis);

        int prevMidnightIndex = getClosestMidnightValIndex(hive, today, 0);
        int prevprevMidnightIndex = getClosestMidnightValIndex(hive, yesterday, hive.getMeasurements().size() - prevMidnightIndex - 1);
        Timestamp prevTime = hive.getMeasurements().get(prevMidnightIndex).getTimestamp();
        Timestamp prevprevTime = hive.getMeasurements().get(prevprevMidnightIndex).getTimestamp();

        if (!(isAroundMidnight(prevTime, today) && isAroundMidnight(prevprevTime, yesterday))) {
            hive.setWeightDelta(Double.NaN);
        } else {
            double prevMidnightWeight = hive.getMeasurements().get(prevMidnightIndex).getWeight();
            double prevprevMidnightWeight = hive.getMeasurements().get(prevprevMidnightIndex).getWeight();
            double deltaWeight = prevMidnightWeight - prevprevMidnightWeight;
            hive.setWeightDelta(deltaWeight);
        }
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

    public void subscribeHive(int id) {
        subscriptionManager.saveSubscription(id);
    }

    public ArrayList<Integer> getSubscriptionIDs() {
        return subscriptionManager.getSubscriptions();
    }

    public void unsubscribeHive(int id) {
        subscriptionManager.deleteSubscription(id);
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

}
