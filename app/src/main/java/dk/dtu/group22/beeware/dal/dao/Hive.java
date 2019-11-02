package dk.dtu.group22.beeware.dal.dao;

import java.util.List;

public class Hive {
    private int id;
    private String name;
    private List<Measurement> measurements;
    private double weightDelta;
    private double currWeight;
    private int weightStatus;
    private int tempStatus;
    private int humidStatus;
    private int illumStatus;

    @Override
    public String toString() {
        return "Hive{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", measurements=" + measurements +
                ", weightDelta=" + weightDelta +
                ", currWeight=" + currWeight +
                ", weightStatus=" + weightStatus +
                ", tempStatus=" + tempStatus +
                ", humidStatus=" + humidStatus +
                ", illumStatus=" + illumStatus +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrWeight() {
        return currWeight;
    }

    public void setCurrWeight(double currWeight) {
        this.currWeight = currWeight;
    }

    public int getWeightStatus() {
        return weightStatus;
    }

    public void setWeightStatus(int weightStatus) {
        this.weightStatus = weightStatus;
    }

    public int getTempStatus() {
        return tempStatus;
    }

    public void setTempStatus(int tempStatus) {
        this.tempStatus = tempStatus;
    }

    public int getHumidStatus() {
        return humidStatus;
    }

    public void setHumidStatus(int humidStatus) {
        this.humidStatus = humidStatus;
    }

    public int getIllumStatus() {
        return illumStatus;
    }

    public void setIllumStatus(int illumStatus) {
        this.illumStatus = illumStatus;
    }

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
