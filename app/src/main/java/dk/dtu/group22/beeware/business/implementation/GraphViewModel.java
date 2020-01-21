package dk.dtu.group22.beeware.business.implementation;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public class GraphViewModel extends ViewModel {
    // This class handles all logic for displaying the graph in GraphActivity

    private final String TAG = "GraphViewModel";
    private Logic logic = Logic.getSingleton();
    private float leftAxisMin, leftAxisMax, rightAxisMin, rightAxisMax, illumMax, illumMin;
    private Hive hive;
    private int timeDelta = 1000 * 3600 * 24 * 7;
    private Timestamp toDate = new Timestamp(new Date().getTime());
    private Timestamp fromDate = new Timestamp(toDate.getTime() - timeDelta);
    private boolean backgroundDownloadInProgress = false;

    // State of visibility
    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false, zoomEnabled = true;

    /**
     * Resets the clock on a Timestamp
     *
     * @param stamp
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
        Log.d(TAG, "roundDateToMidnight: Corrected the date to midnight: " + result);
        return result;
    }

    /**
     * Gets a hive object from Logic, updates local object to this value.
     * @param id
     */
    public void downloadHiveData(int id) {
        fromDate = roundDateToMidnight(fromDate);
        hive = logic.getHive(id, fromDate, new Timestamp(System.currentTimeMillis()));
        Log.d(TAG, "downloadHiveData: Downloaded hive data for hive " +
                id + " from" + fromDate + " to " + toDate + ".");
    }

    /**
     * Downloads hive data for one year back in background, updates local object
     * @param id
     */
    public void downloadOldDataInBackground(int id) {

        backgroundDownloadInProgress = true;
        System.out.println("downloadOldDataInBackground: Starting background download.");

        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endDate.getTime());
        cal.add(Calendar.MONTH, -3);
        Timestamp startDate = new Timestamp(cal.getTimeInMillis());
        startDate = roundDateToMidnight(startDate);

        for (int i = 0; i < 7; i++) {

            Timestamp a = new Timestamp(startDate.getTime());
            Timestamp b = new Timestamp(endDate.getTime());
            Hive junk = null;

            while (junk == null) {
                junk = logic.getHive(id, a, b);
            }
            System.out.println("downloadOldDataInBackground: Downloaded Hive " + id + ", " +
                    "from " + a.toString().substring(0, 10) + " " +
                    "to " + b.toString().substring(0, 10) + ".");

            // Iterate backwards
            endDate = new Timestamp(startDate.getTime());
            cal.setTimeInMillis(endDate.getTime());
            cal.add(Calendar.MONTH, -2);
            startDate = new Timestamp(cal.getTimeInMillis());
        }
        backgroundDownloadInProgress = false;
        Log.d(TAG, "downloadOldDataInBackground: Background download Done.");
    }

    /**
     * Sets local max or min values based on the data's max and min value (v) in selected period.
     * @param axis
     * @param v
     */
    private void checkMaxMin(float v, char axis) {
        if (axis == 'l' && v != 0.0) {
            if (v > leftAxisMax) {
                leftAxisMax = v + 1;
            } else if (v < leftAxisMin) {
                leftAxisMin = v - 1;
            }
        } else if (axis == 'r' && v != 0) {
            if (v > rightAxisMax) {
                rightAxisMax = v + 1;
            } else if (v < rightAxisMin && v >= 0) {
                rightAxisMin = v - 1;
            }
        } else if (axis == 'i' && v != 0) {
            // i for illuminance
            if (v > illumMax) {
                illumMax = v + 1;
            } else if (v < illumMin) {
                illumMin = v - 1;
            }
        }
    }

    /**
     * Checks if a timestamp is between fromDate and toDate
     *
     * @param timeStamp
     * @return True if time is in period
     */
    private boolean isInInterval(Timestamp timeStamp) {
        return timeStamp.getTime() >= fromDate.getTime() && timeStamp.getTime() <= toDate.getTime();
    }

    /**
     * Calculates period length. This can be used to make an average of n data points, or make a
     * decision to keep only data from midnight.
     * @return true if midnight data should be used
     */
    public boolean useMidnightData() {
        double days = (toDate.getTime() - fromDate.getTime()) / 86400000; // Millis in a day
        System.out.println("Days requested: " + days);
        if (days > 32) {
            System.out.println("Using midnight data. Days requested: " + days);
            // Midnight data
            return true;
        }
        return false;
    }

    /**
     * Extracts weight from midnight.
     *
     * @return List of weight values as MPAndroidChart Entries
     */
    public List<Entry> extractMidnightWeight() {
        boolean foundMidnight = false;
        List<Entry> res = new ArrayList<>();
        leftAxisMin = 99;
        leftAxisMax = -99;
        Calendar cal = Calendar.getInstance();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            cal.setTimeInMillis((long) time);
            if (isInInterval(measure.getTimestamp()) && cal.get(Calendar.HOUR_OF_DAY) == 0 && !foundMidnight) {
                // If this data is in requested interval
                float weight = (float) measure.getWeight();
                res.add(new Entry(time, weight));
                checkMaxMin(weight, 'l');
                foundMidnight = true;
            } else if (foundMidnight && cal.get(Calendar.HOUR_OF_DAY) != 0) {
                foundMidnight = false;
            }
        }
        return res;
    }

    /**
     * Extracts midday temperature.
     *
     * @return A list of Temperature values as Entries
     */
    public List<Entry> extractMiddayTemperature() {
        List<Entry> res = new ArrayList<>();
        rightAxisMin = 99;
        rightAxisMax = -99;
        boolean foundMidday = false;
        Calendar cal = Calendar.getInstance();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            cal.setTimeInMillis((long) time);
            if (isInInterval(measure.getTimestamp()) && cal.get(Calendar.HOUR_OF_DAY) == 12 && !foundMidday) {
                // If this data is in requested interval
                float temp = (float) measure.getTempIn();
                res.add(new Entry(time, temp));
                checkMaxMin(temp, 'r');
                foundMidday = true;
            } else if (foundMidday && cal.get(Calendar.HOUR_OF_DAY) != 12) {
                foundMidday = false;
            }
        }
        return res;
    }

    /**
     * Extracts illuminance from three daily points
     *
     * @return A list of logarithmic illuminance values as Entries
     */
    public List<Entry> extractThreeDailyPointsIlluminance() {
        List<Entry> res = new ArrayList<>();
        // Find max and min
        for (Measurement measure : hive.getMeasurements()) {
            if (isInInterval(measure.getTimestamp())) {
                float illum = (float) (measure.getIlluminance() + 1.2);
                if (illum > 0) {
                    illum = (float) Math.log(illum);
                    checkMaxMin(illum, 'i');
                }
            }
        }
        float range = rightAxisMax - rightAxisMin;
        // Populate list
        boolean foundPoint = false;
        Calendar cal = Calendar.getInstance();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            cal.setTimeInMillis((long) time);
            if (isInInterval(measure.getTimestamp())
                    && cal.get(Calendar.HOUR_OF_DAY) == 12 || cal.get(Calendar.HOUR_OF_DAY) == 15 || cal.get(Calendar.HOUR) == 9
                    && !foundPoint) {
                float illum = (float) (measure.getIlluminance() + 1.1);
                if (illum > 0) {
                    illum = (float) Math.log(illum);
                    res.add(new Entry(time, (illum / illumMax) * (range) + rightAxisMin));
                } else {
                    res.add(new Entry(time, 0));
                }
                foundPoint = true;
            } else if (foundPoint && cal.get(Calendar.HOUR_OF_DAY) != 12) {
                foundPoint = false;
            }
        }
        return res;
    }

    /**
     * Extracts humidity from midday
     *
     * @return A list of humidity values as Entries
     */
    public List<Entry> extractMiddayHumidity() {
        List<Entry> res = new ArrayList<>();
        boolean foundMidday = false;
        Calendar cal = Calendar.getInstance();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            cal.setTimeInMillis((long) time);
            if (isInInterval(measure.getTimestamp()) && cal.get(Calendar.HOUR_OF_DAY) == 12 && !foundMidday) {
                float humid = (float) measure.getHumidity();
                res.add(new Entry(time, (humid / 102) * (rightAxisMax - rightAxisMin) + rightAxisMin)); // Percentage of matrix height
            } else if (foundMidday && cal.get(Calendar.HOUR_OF_DAY) != 12) {
                foundMidday = false;
            }
        }
        return res;
    }

    /**
     * Extract weight, all data points
     *
     * @return A list of weight values as Entries
     */
    public List<Entry> extractWeight() {
        List<Entry> res = new ArrayList<>();
        leftAxisMin = 99;
        leftAxisMax = -99;
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                // If this data is in requested interval
                float weight = (float) measure.getWeight();
                res.add(new Entry(time, weight));
                checkMaxMin(weight, 'l');
            }
        }
        return res;
    }

    /**
     * Extract temperature, all data points
     *
     * @return A list of temperature values as Entries
     */
    public List<Entry> extractTemperature() {
        List<Entry> res = new ArrayList<>();
        rightAxisMin = 99;
        rightAxisMax = -99;
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                // If this data is in requested interval
                float temp = (float) measure.getTempIn();
                if (temp < 0) {
                    temp = 0;
                }
                res.add(new Entry(time, temp));
                checkMaxMin(temp, 'r');
            }
        }
        return res;
    }

    /**
     * Extract illuminance, all data points
     *
     * @return A list of illuminance values as Entries
     */
    public List<Entry> extractIlluminance() {
        List<Entry> res = new ArrayList<>();
        // Find max and min
        for (Measurement measure : hive.getMeasurements()) {
            if (isInInterval(measure.getTimestamp())) {
                float illum = (float) (measure.getIlluminance() + 1.2);
                if (illum > 0) {
                    illum = (float) Math.log(illum);
                    checkMaxMin(illum, 'i');
                }
            }
        }
        float range = rightAxisMax - rightAxisMin;
        // Populate list
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                float illum = (float) (measure.getIlluminance() + 1.1);
                if (illum > 0) {
                    illum = (float) Math.log(illum);
                    res.add(new Entry(time, (illum / illumMax) * (range) + rightAxisMin));
                } else {
                    res.add(new Entry(time, 0));
                }
            }
        }
        return res;
    }

    /**
     * Extract humidity, all data points
     *
     * @return A list of humidity values as Entries
     */
    public List<Entry> extractHumidity() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                float humid = (float) measure.getHumidity();
                res.add(new Entry(time, (humid / 102) * (rightAxisMax - rightAxisMin) + rightAxisMin)); // Percentage of matrix height
            }
        }
        return res;
    }

    /**
     * @param hive
     * @param start
     * @param end
     * @return A list containing measurements, in correct order, from start to end.
     */
    // Filters time intervals from a list,
    // Only picks midnight when time interval is greater than 24hours
    public List<Measurement> filterTimeInterval(Hive hive, Timestamp start, Timestamp end) {
        //TODO: Optimize with binary search.
        List<Measurement> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            Timestamp t = measure.getTimestamp();
            // start <= t <= end
            if (start.compareTo(t) <= 0 && t.compareTo(end) <= 0) {
                res.add(measure);
            }
        }
        return res;
    }

    /**
     * Split a list such that when delta between two timestamps points is big, then we create
     * a  new list element.
     *
     * @param dataset A dataset, where the x coordinate is a timestamp represented in nanoseconds.
     * @param delta   nanosecond time
     * @return A list of lists, which are non-overlapping intervals, where the end of list element i,
     * is at least delta time away from list element i+1, for any 0<= i < dataset.size()-1
     */
    public List<List<Entry>> makeMultiListBasedOnDelta(List<Entry> dataset, long delta) {
        List<List<Entry>> res = new ArrayList<>();
        int start = 0;
        int end = 0;
        for (int i = 0; i < dataset.size() - 1; ++i) {
            float t1 = dataset.get(i).getX();
            float t2 = dataset.get(i + 1).getX();
            if (t2 - t1 >= delta || i == dataset.size() - 2) {
                end = i + 1;
                List<Entry> newList = makeCopyOfInterval(dataset, start, end);
                res.add(newList);
                start = end;
            }
        }
        return res;
    }

    /**
     * @param data
     * @param start
     * @param end
     * @return a list of entries, which is a deep copy of the interval specified by start and end.
     * Of course following Dijkstra indexing intervals [start; end[.
     */
    public List<Entry> makeCopyOfInterval(List<Entry> data, int start, int end) {
        List<Entry> res = new ArrayList<>(end - start);
        for (int i = start; i < end; ++i) {
            res.add(data.get(i));
        }
        return res;
    }

    public float getLeftAxisMin() {
        return leftAxisMin;
    }

    public float getLeftAxisMax() {
        return leftAxisMax;
    }

    public float getRightAxisMin() {
        return rightAxisMin;
    }

    public float getRightAxisMax() {
        return rightAxisMax;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }

    public Hive getHive() {
        return hive;
    }

    public boolean isWeightLineVisible() {
        return weightLineVisible;
    }

    public void setWeightLineVisible(boolean weightLineVisible) {
        this.weightLineVisible = weightLineVisible;
    }

    public boolean isTemperatureLineVisible() {
        return temperatureLineVisible;
    }

    public void setTemperatureLineVisible(boolean temperatureLineVisible) {
        this.temperatureLineVisible = temperatureLineVisible;
    }

    public boolean isSunlightLineVisible() {
        return sunlightLineVisible;
    }

    public void setSunlightLineVisible(boolean sunlightLineVisible) {
        this.sunlightLineVisible = sunlightLineVisible;
    }

    public boolean isHumidityLineVisible() {
        return humidityLineVisible;
    }

    public void setHumidityLineVisible(boolean humidityLineVisible) {
        this.humidityLineVisible = humidityLineVisible;
    }

    public void updateTimePeriod(Timestamp from, Timestamp to) {
        this.fromDate = from;
        this.toDate = to;
    }

    // Used to set lower and upper X value visible
    public Timestamp getFromDate() {
        return fromDate;
    }

    public Timestamp getToDate() {
        return toDate;
    }

    public boolean isBackgroundDownloadInProgress() {
        return backgroundDownloadInProgress;
    }

}
