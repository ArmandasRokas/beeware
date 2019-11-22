package dk.dtu.group22.beeware.dal.dao;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Immutable Measurement instance. Does not need setters.
 */
public class Measurement implements Serializable {
    private Timestamp timestamp;
    private double weight;
    private double tempIn;

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setTempIn(double tempIn) {
        this.tempIn = tempIn;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setIlluminance(double illuminance) {
        this.illuminance = illuminance;
    }

    private double humidity;
    private double illuminance;
    public Measurement(Timestamp timestamp, double weight, double tempIn, double humidity, double illuminance){
        this.timestamp = timestamp;
        this.weight = weight;
        this.tempIn = tempIn;
        this.humidity = humidity;
        this.illuminance = illuminance;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "timestamp=" + timestamp +
                ", weight=" + weight +
                ", tempIn=" + tempIn +
                ", humidity=" + humidity +
                ", illuminance=" + illuminance +
                '}';
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public double getWeight() {
        return weight;
    }

    public double getTempIn() {
        return tempIn;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getIlluminance() {
        return illuminance;
    }
}
