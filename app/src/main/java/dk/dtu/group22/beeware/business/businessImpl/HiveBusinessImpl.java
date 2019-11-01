package dk.dtu.group22.beeware.business.businessImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveSubscriptionRepository;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveSubscriptionRepoHiveToolImpl;
import dk.dtu.group22.beeware.data.repositories.repoImpl.UserRepoArrayListImpl;

public class HiveBusinessImpl implements HiveBusiness {

    private HiveRepository hiveRepo;
    private UserRepository userRepository;
    private HiveSubscriptionRepository hiveSubscriptionRepository;

    public HiveBusinessImpl() {
        this.hiveRepo = new HiveRepoHiveToolImpl();
        this.userRepository = new UserRepoArrayListImpl();
        this.hiveSubscriptionRepository = new HiveSubscriptionRepoHiveToolImpl();
    }

    @Override
    public List<Hive> getHives(User user, int daysDelta) {
        // TODO implement daysDelta. Armandas. Days delta is how many days back needs to fetched from today
        long now = System.currentTimeMillis();
        long since = now - (86400000 * daysDelta);
        List<Hive> subscribedHives = userRepository.getSubscribedHives(user);
        List<Hive> hivesWithMeasurements = new ArrayList<>();
        for (Hive hive : subscribedHives) {
            Hive h = hiveRepo.getHive(hive, new Timestamp(since), new Timestamp(now));
            if (h == null) {
                throw new HiveNotFound("Hive with id " + hive.getId() + " does not exits.");
            } else {
                h = calculateCurrValuesAndStatus(h);
                hivesWithMeasurements.add(h);
            }


        }
        return hivesWithMeasurements;
    }

    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        return hiveRepo.getHive(hive, sinceTime, untilTime);
    }

    private Hive calculateCurrValuesAndStatus(Hive hive) {
        hive.setCurrWeight(hive.getMeasurements().get(hive.getMeasurements().size() - 1).getWeight());
        System.out.println(hive.getMeasurements().get(0).getTimestamp().toString().substring(8, 10));

        Calendar currentDate = Calendar.getInstance();

        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(currentDate.getTimeInMillis() - twentyFourHoursInMillis);

        System.out.println(currentDate.getTime());
        System.out.println(yesterday.getTime());
        int prevMidnightIndex = getClosestMidnightValIndex(hive, currentDate, 0);
        int prevprevMidnightIndex = getClosestMidnightValIndex(hive, yesterday, prevMidnightIndex);

        double prevMidnightWeight = hive.getMeasurements().get(prevMidnightIndex).getWeight();
        double prevprevMidnightWeight = hive.getMeasurements().get(prevprevMidnightIndex).getWeight();
        double deltaWeight = prevMidnightWeight - prevprevMidnightWeight;
        hive.setWeightDelta(deltaWeight);
        return hive;
    }

    /**
     * @param hive
     * @param day        The day we want to find the (most recently occurring) midnight for. More specifically if you had day x at clock 22:10 it will find the index corrosponding to the closest
     * @param startIndex Where to start to search in the data, from the end of the Hive.measurements list. Distance from end of list. 0 is equivalent to end of list.
     * @return index in the hive.measurments list, where the timestamp is the closest to the most recently occuring midnight, before the day variable.
     */
    private int getClosestMidnightValIndex(Hive hive, Calendar day, int startIndex) {
        Calendar idealPrevMidnight = getMidnightInstanceOfDay(day);
        System.out.println("Day: " + day.getTime());
        System.out.println("Idealmidnight: "+idealPrevMidnight.getTime());
        Timestamp closest_midnight_value = hive.getMeasurements().get(hive.getMeasurements().size() - 1 - startIndex).getTimestamp();
        Long smallestDeltaFromMidnight = Math.abs(idealPrevMidnight.getTimeInMillis() - closest_midnight_value.getTime());
        int res = startIndex;
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
        System.out.println("Found midnight: "+ hive.getMeasurements().get(res).getTimestamp());
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


    @Override
    public void subscribeHive(User user, Hive hive) {
        try {
            userRepository.subscribeHive(user, hive);
        } catch (Exception e) {
            // TODO add exception handling
            e.printStackTrace();
        }
    }

    @Override
    public List<Hive> getHivesToSubscribe() {
        try {
            List<Hive> hives = hiveSubscriptionRepository.getHivesToSubscribe();
            if (hives == null || hives.isEmpty()) {
                throw new HivesToSubscribeNoFound("Business error. Unable to fetch data");
            } else {
                return hives;
            }
        } catch (Exception e) {
            throw new HivesToSubscribeNoFound(e.getMessage());
        }
    }

    public UserRepository getUserRepository() {
        return this.userRepository;
    }
}
