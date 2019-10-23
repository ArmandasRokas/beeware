package dk.dtu.group22.beeware.view;

import androidx.lifecycle.ViewModel;

public class GraphViewModel extends ViewModel {
    // Serves as a temporary "state" for when the GraphActivity is redrawn.

    private boolean weightLineVisible = true, temperatureLineVisible = false,
            sunlightLineVisible = false, humidityLineVisible = false;

    // TODO: Update these with real data:
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

    public float getDefaultZoom() {
        return defaultZoom;
    }

    public void setDefaultZoom(float defaultZoom) {
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
}
