package dk.dtu.group22.beeware.data.entities;

import java.sql.Timestamp;

public class Measurement {
    private Timestamp timestamp;
    private double weight;
    private double tempIn;
    private double humidity;
    private double illuminance;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getTempIn() {
        return tempIn;
    }

    public void setTempIn(double tempIn) {
        this.tempIn = tempIn;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(double illuminance) {
        this.illuminance = illuminance;
    }
}
