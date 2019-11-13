package dk.dtu.group22.beeware.business.implementation;

import androidx.core.util.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dto.implementation.HiveHivetool;
import dk.dtu.group22.beeware.dal.dto.implementation.SubscriptionHivetool;
import dk.dtu.group22.beeware.dal.dto.implementation.UserHiveIds;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dto.interfaces.IUser;
import dk.dtu.group22.beeware.dal.dto.interfaces.NameIdPair;

public class Logic implements ILogic {

    private HiveHivetool hiveHivetool;
    private IUser userHiveIds;
    private ISubscription subscriptionHivetool;


    public Logic() {
        this.hiveHivetool = new HiveHivetool();
        this.userHiveIds = new UserHiveIds();
        this.subscriptionHivetool = new SubscriptionHivetool();
    }

    @Override
    public List<Hive> getHives(int daysDelta) {
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Integer> subscribedHives = userHiveIds.getSubscribedIds();
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

        return createHive(id, sinceTime, untilTime);
    }

    public Hive createHive(int id, Timestamp sinceTime, Timestamp untilTime) {
        Hive hive = new Hive();
        hive.setId(id);

        Pair<List<Measurement>, String> measurementsAndName = hiveHivetool.getHiveMeasurements(id, sinceTime, untilTime);

        hive.setMeasurements(measurementsAndName.first);
        hive.setName(measurementsAndName.second);

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
        int prevprevMidnightIndex = getClosestMidnightValIndex(hive, yesterday, hive.getMeasurements().size() - prevMidnightIndex-1);
        Timestamp prevTime = hive.getMeasurements().get(prevMidnightIndex).getTimestamp();
        Timestamp prevprevTime = hive.getMeasurements().get(prevprevMidnightIndex).getTimestamp();

        if(!(isAroundMidnight(prevTime, today) && isAroundMidnight(prevprevTime, yesterday))){
           hive.setWeightDelta(Double.NaN);
        }else {
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

    private boolean isAroundMidnight(Timestamp time, Calendar day){
        Calendar idealMidnight = getMidnightInstanceOfDay(day);
        int hourInMillis =  60*60*1000;
        long beforeMidnight = idealMidnight.getTimeInMillis() - hourInMillis;
        long afterMidnight = idealMidnight.getTimeInMillis() + hourInMillis;
        if(beforeMidnight <= time.getTime() && time.getTime() <= afterMidnight){
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

    public void subscribeHive(int id) {
        userHiveIds.subscribeHive(id);
    }

    @Override
    public List<NameIdPair> getHivesToSubscribe() {
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

    public IUser getUserHiveIds() {
        return userHiveIds;
    }
}
