package dk.dtu.group22.beeware.presentation;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public class GraphViewModel extends ViewModel {
    // TODO: Fjern udkommenteret kode
    //private final int useOnlyNth = 4;
    private final String TAG = "GraphViewModel";
    private Logic logic = Logic.getSingleton();
    private float leftAxisMin, leftAxisMax, rightAxisMin, rightAxisMax;
    private Hive hive;
    private int timeDelta = 1000 * 3600 * 24 * 7;
    private Timestamp toDate = new Timestamp(new Date().getTime());
    private Timestamp fromDate = new Timestamp(toDate.getTime() - timeDelta);
    private boolean backgroundDownloadInProgress = false;

    // State
    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false, zoomEnabled = true;

    // Data handling for graph
    public void downloadHiveData(int id) {
        hive = logic.getHive(id, fromDate, new Timestamp(System.currentTimeMillis()));
        Log.d(TAG, "downloadHiveData: Downloaded hive data for hive " +
                id + " from" + fromDate + " to " + toDate + ".");
    }

    // Set max and min values based on data
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
            } else if (v < rightAxisMin) {
                rightAxisMin = v - 1;
            }
        }
    }

    // Scale any data to x axis (log)
    private float scaleNumToLeftAxis(float min, float max, float in) {

        if (in <= 0) {
            return 0;
        }
        return (float) Math.log(in) * (max - min) / 20 + min;
    }

    private boolean isInInterval(Timestamp t) {
        return t.getTime() >= fromDate.getTime() && t.getTime() <= toDate.getTime();
    }

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

    public List<Entry> extractTemperature() {
        List<Entry> res = new ArrayList<>();
        rightAxisMin = 99;
        rightAxisMax = -99;
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                // If this data is in requested interval
                float temp = (float) measure.getTempIn();
                res.add(new Entry(time, temp));
                checkMaxMin(temp, 'r');
            }
        }
        return res;
    }

    public List<Entry> extractIlluminance() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                float illum = (float) measure.getIlluminance();
                res.add(new Entry(time, scaleNumToLeftAxis(leftAxisMin, leftAxisMax, illum)));
            }
        }
        return res;
    }

    public List<Entry> extractHumidity() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            if (isInInterval(measure.getTimestamp())) {
                float humid = (float) measure.getHumidity();
                //res.add(new Entry(time, ((humid - 30) / 150 * (leftAxisMax - leftAxisMin) + leftAxisMin)));
                res.add(new Entry(time, (humid / 102) * (leftAxisMax - leftAxisMin))); // Percentage of matrix height
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

    // Used to set lower and upper X vale visible
    public Timestamp getFromDate() {
        return fromDate;
    }

    public Timestamp getToDate() {
        return toDate;
    }

    public void downloadOldDataInBackground(int id) {
        backgroundDownloadInProgress = true;
        System.out.println("downloadOldDataInBackground: Starting background download.");

        Timestamp endDate = new Timestamp(System.currentTimeMillis());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endDate.getTime());
        cal.add(Calendar.MONTH, -3);
        Timestamp startDate = new Timestamp(cal.getTimeInMillis());

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

    public boolean isBackgroundDownloadInProgress() {
        return backgroundDownloadInProgress;
    }

    public float getGranularity(char yAxis) {
        // Custom granularity (numeric distance between labels on y axis).

        /*First draft:
         * Round the distance between axis max and min up to something easily divisible by 6,
         * e.g 12 , 3, or 1.2, and set (left/right) axismax and axismin accordingly. This could be
         * defined in a list or by an algorithm. Then set axis granularity to a 6th of this number.
         * The challenge is to define arbitrary "round numbers" for any scale. It's important
         * that the axis minimum is also a round number by the same definition.
         *
         * This must be called after making the linedataset (where we get max and min),
         * but before rendering.*/

        return 0;
    }
}
