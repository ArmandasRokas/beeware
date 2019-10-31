package dk.dtu.group22.beeware.view;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;

public class GraphViewModel extends ViewModel {
    private final String TAG = "GraphViewModel";
    private HiveBusiness hiveBusiness = new HiveBusinessImpl();

    private Hive hive;

    // State
    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false;

    // TODO: Update zoom settings with real data:
    // Center at last value in array to show current time.
    // Default Zoom is (all data points) / (how many data points you want to see).
    // (e.g. Year / Week)
    private float xCenter = 365, defaultZoom = 365 / 7;

    public float getxCenter() {
        return xCenter;
    }

    public void setxCenter(float xCenter) {
        this.xCenter = xCenter;
    }

    public float getZoom() {
        return defaultZoom;
    }

    public void setZoom(float defaultZoom) {
        this.defaultZoom = defaultZoom;
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

    // Data handling for graph
    public void downloadHiveData(Hive tempHive) {
        // TODO: Pass the selected hive!
        if (hive == null || hive.getMeasurements() == null) {
            hive = hiveBusiness.getHive(tempHive, new Timestamp(0), new Timestamp(System.currentTimeMillis()));
            Log.d(TAG, "downloadHiveData: Downloading hive data.");
        }
    }


    public List<Entry> extractWeight() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float weight = (float) measure.getWeight();
            res.add(new Entry(time, weight));
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
            res.add(new Entry(time, illum));
        }
        return res;
    }

    public List<Entry> extractHumidity() {
        List<Entry> res = new ArrayList<>();
        for (Measurement measure : hive.getMeasurements()) {
            float time = (float) measure.getTimestamp().getTime();
            float humid = (float) measure.getHumidity();
            res.add(new Entry(time, humid));
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
}
