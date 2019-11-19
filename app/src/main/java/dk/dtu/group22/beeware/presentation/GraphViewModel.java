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
    private final String TAG = "GraphViewModel";
    private Logic logic = Logic.getSingleton();
    private float leftAxisMin, leftAxismax, rightAxisMin, rightAxisMax;
    private Hive hive;
    private long fromDate = (long) 1000 * 3600 * 24 * 7 * 9;

    // State
    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false, zoomEnabled = true;

    // Center at last value in array to show current time.
    Date date = new Date();
    private float xCenter = date.getTime(), pointsVisible;

    // Data handling for graph
    public void downloadHiveData(int id) {
        hive = logic.getHive(id, new Timestamp(System.currentTimeMillis() - fromDate), new Timestamp(System.currentTimeMillis()));
        Log.d(TAG, "downloadHiveData: Downloaded hive data for hive " + id + ".");
    }

    // TODO: Set max values and scale illuminance and humidity

    public List<Entry> extractWeight() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float weight = (float) measure.getWeight();
            res.add(new Entry(time, weight));
            //Log.d(TAG, "extractWeight: TEST: "  + weight);
        }
        return res;
    }

    public List<Entry> extractTemperature() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float temp = (float) measure.getTempIn();
            res.add(new Entry(time, temp));
        }
        return res;
    }

    public List<Entry> extractIlluminance() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float illum = (float) measure.getIlluminance();
            res.add(new Entry(time, scaleNumToLeftAxis(leftAxisMin, illum)));
            Log.d(TAG, "extractIlluminance: " + illum);
        }
        return res;
    }

    public List<Entry> extractHumidity() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float humid = (float) measure.getHumidity();
            res.add(new Entry(time, leftAxisMin + (humid / 7)));
            //Log.d(TAG, "extractHumidity: humid = " + humid);
        }
        return res;
    }

    private float scaleNumToLeftAxis(float min, float in) {
        if (in <= 0) {
            return in;
        }
        return 2 * (float) Math.log(in) + min;
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

    public float getLeftAxisMin() {
        return leftAxisMin;
    }

    public void setLeftAxisMin(float leftAxisMin) {
        this.leftAxisMin = leftAxisMin;
    }

    public float getLeftAxismax() {
        return leftAxismax;
    }

    public void setLeftAxismax(float leftAxismax) {
        this.leftAxismax = leftAxismax;
    }

    public float getRightAxisMin() {
        return rightAxisMin;
    }

    public void setRightAxisMin(float rightAxisMin) {
        this.rightAxisMin = rightAxisMin;
    }

    public float getRightAxisMax() {
        return rightAxisMax;
    }

    public void setRightAxisMax(float rightAxisMax) {
        this.rightAxisMax = rightAxisMax;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }

    public float getxCenter() {
        return xCenter;
    }

    public void setxCenter(float xCenter) {
        this.xCenter = xCenter;
    }

    public float getZoom() {
        // The number "zoom" is total / how many you want to see
        return hive.getMeasurements().size() / pointsVisible;
    }

    public void setZoom(float pointsVisible) {
        this.pointsVisible = pointsVisible;
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
}
