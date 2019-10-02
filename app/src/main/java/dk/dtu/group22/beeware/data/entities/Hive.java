package dk.dtu.group22.beeware.data.entities;

import java.util.List;

public class Hive {
    private int id;
    private List<Measurement> measurements;
    private double weightDelta;
    private double currWeight;

    public double getWeightDelta() {
        return weightDelta;
    }

    public void setWeightDelta(double weightDelta) {
        this.weightDelta = weightDelta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }
}
