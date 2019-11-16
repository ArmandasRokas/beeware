package dk.dtu.group22.beeware.dal.dao;

import java.util.ArrayList;
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
    private Status weightStatus = Status.UNDEFINED;
    private Status tempStatus = Status.UNDEFINED;
    private Status humidStatus = Status.UNDEFINED;
    private Status illumStatus = Status.UNDEFINED;
    private List<StatusIntrospection> statusIntrospection;

    public Hive(int id, String name) {
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

    public List<StatusIntrospection> getStatusIntrospection() {
        return new ArrayList<StatusIntrospection>(this.statusIntrospection);
    }

    public void setStatusIntrospection(List<StatusIntrospection> statusIntrospection) {
        this.statusIntrospection = statusIntrospection;
    }

    public enum Status {
        UNDEFINED,
        DANGER,
        WARNING,
        OK
    }

    public enum Variables {
        WEIGHT,
        ILLUMINANCE,
        HUMIDITY,
        TEMPERATURE,
        OTHER
    }

    // This class is used to inspect the reasoning behind different variables status state and why
    // they are set the values that they are.
    public static class StatusIntrospection {
        private Variables variable;
        private Status status;
        private String reasoning;

        public StatusIntrospection(Variables variable, Status status, String reasoning) {
            this.variable = variable;
            this.status = status;
            this.reasoning = reasoning;
        }

        public Variables getVariable() {
            return variable;
        }

        public Status getStatus() {
            return status;
        }

        public String getReasoning() {
            return reasoning;
        }
    }

}
