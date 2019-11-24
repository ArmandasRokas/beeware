package dk.dtu.group22.beeware.presentation;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;

public class GraphViewModel extends ViewModel {
    private final int useOnlyNth = 4;
    private final String TAG = "GraphViewModel";
    private Logic logic = Logic.getSingleton();
    private float xAxisMin = 40, xAxisMax = 0, yAxisMin = 30, yAxisMax = 0;
    private Hive hive;
    private int timeDelta = 1000 * 3600 * 24 * 7;
    private Timestamp toDate = new Timestamp(new Date().getTime());
    private Timestamp fromDate = new Timestamp(toDate.getTime() - timeDelta);



    // State
    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false, zoomEnabled = true;

    // Data handling for graph
    public void downloadHiveData(int id) {
        hive = logic.getHive(id, fromDate, toDate);
        Log.d(TAG, "downloadHiveData: Downloaded hive data for hive " +
                id + " from" + fromDate + " to " + toDate + ".");
    }

    // Set max and min values based on data
    private void checkMaxMin(float v, char axis) {
        if (axis == 'x' && v > 1) {
            if (v > xAxisMax) {
                xAxisMax = v + 2;
            } else if (v < xAxisMin) {
                xAxisMin = v - 2;
            }
        } else if (axis == 'y' && v != 0) {
            if (v > yAxisMax) {
                yAxisMax = v + 2;
            } else if (v < yAxisMin) {
                yAxisMin = v - 2;
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

    public List<Entry> extractWeight() {
        List<Entry> res = new ArrayList<>();
        int i = 0;
        for (Measurement measure : hive.getMeasurements()) {
            if (i % useOnlyNth == 0) {
                float time = (float) measure.getTimestamp().getTime();
                float weight = (float) measure.getWeight();
                res.add(new Entry(time, weight));
                checkMaxMin(weight, 'x');
                //Log.d(TAG, "extractWeight: TEST: "  + weight);
            }
            i++;
        }
        return res;
    }

    public List<Entry> extractTemperature() {
        List<Entry> res = new ArrayList<>();
        int i = 0;
        for (Measurement measure : hive.getMeasurements()) {
            if (i % useOnlyNth == 0) {
                float time = (float) measure.getTimestamp().getTime();
                float temp = (float) measure.getTempIn();
                res.add(new Entry(time, temp));
                checkMaxMin(temp, 'y');
            }
            i++;
        }
        return res;
    }

    public List<Entry> extractIlluminance() {
        List<Entry> res = new ArrayList<>();
        int i = 0;
        for (Measurement measure : hive.getMeasurements()) {
            if (i % useOnlyNth == 0) {
                float time = (float) measure.getTimestamp().getTime();
                float illum = (float) measure.getIlluminance();
                res.add(new Entry(time, scaleNumToLeftAxis(xAxisMin, xAxisMax, illum)));
                //Log.d(TAG, "extractIlluminance: " + illum);
            }
            i++;
        }
        return res;
    }

    public List<Entry> extractHumidity() {
        List<Entry> res = new ArrayList<>();
        int i = 0;
        for (Measurement measure : hive.getMeasurements()) {
            if (i % useOnlyNth == 0) {
                float time = (float) measure.getTimestamp().getTime();
                float humid = (float) measure.getHumidity();
                res.add(new Entry(time, ((humid - 30) / 150 * (xAxisMax - xAxisMin) + xAxisMin)));
                //Log.d(TAG, "extractHumidity: humid = " + humid);
            }
            i++;
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
     * @param dataset A dataset, where the x coordinate is a timestamp represented in nanoseconds.
     * @param delta nanosecond time
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
     *
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

    public float getxAxisMin() {
        return xAxisMin;
    }

    public void setxAxisMin(float xAxisMin) {
        this.xAxisMin = xAxisMin;
    }

    public float getxAxisMax() {
        return xAxisMax;
    }

    public void setxAxisMax(float xAxisMax) {
        this.xAxisMax = xAxisMax;
    }

    public float getyAxisMin() {
        return yAxisMin;
    }

    public void setyAxisMin(float yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    public float getyAxisMax() {
        return yAxisMax;
    }

    public void setyAxisMax(float yAxisMax) {
        this.yAxisMax = yAxisMax;
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
}
