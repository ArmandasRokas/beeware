package dk.dtu.group22.beeware.dal.dao;

import java.util.List;

public class Hive {
    private int id;
    private String name;
    private List<Measurement> measurements;
    private double weightDelta;
    private double currWeight;
    private double currTemp;
    private double currIlluminance;
    private double currHum;
    private Status weightStatus;
    private Status tempStatus;
    private Status humidStatus;
    private Status illumStatus;

    public Hive(int id, String name){
        this.id = id;
        this.name = name;
    }

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

    public double getCurrWeight() {
        return currWeight;
    }

    public void setCurrWeight(double currWeight) {
        this.currWeight = currWeight;
    }

    public Status getWeightStatus() {
        return weightStatus;
    }

    public void setWeightStatus(Status weightStatus) {
        this.weightStatus = weightStatus;
    }

    public Status getTempStatus() {
        return tempStatus;
    }

    public void setTempStatus(Status tempStatus) {
        this.tempStatus = tempStatus;
    }

    public Status getHumidStatus() {
        return humidStatus;
    }

    public void setHumidStatus(Status humidStatus) {
        this.humidStatus = humidStatus;
    }

    public Status getIllumStatus() {
        return illumStatus;
    }

    public void setIllumStatus(Status illumStatus) {
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

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public double getCurrTemp() {
        return currTemp;
    }

    public void setCurrTemp(double currTemp) {
        this.currTemp = currTemp;
    }

    public double getCurrIlluminance() {
        return currIlluminance;
    }

    public void setCurrIlluminance(double currIlluminance) {
        this.currIlluminance = currIlluminance;
    }

    public double getCurrHum() {
        return currHum;
    }

    public void setCurrHum(double currHum) {
        this.currHum = currHum;
    }

    enum Status {
        UNDEFINED,
        DANGER,
        WARNING,
        OK
    }

}
