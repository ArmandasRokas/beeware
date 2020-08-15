package dk.dtu.group22.beeware.dal.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Hive implements Serializable, Comparable<Hive> {
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
    // Critical indicator values
    private int weightIndicator = 15;
    private int tempIndicator = 30;
    private boolean hasBeenConfigured = false;
    private boolean isCachingFinished = false;


    public boolean isCachingFinished() {
        return isCachingFinished;
    }

    public void setCachingFinished(boolean cachingFinished) {
        isCachingFinished = cachingFinished;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String statusToString() {
        return ", weightStatus=" + weightStatus + "\n" +
                ", tempStatus=" + tempStatus + "\n" +
                ", humidStatus=" + humidStatus + "\n" +
                ", illumStatus=" + illumStatus;
    }

    public String getName() {
        return name;
    }

    //Gets the time, at which the hive was last updated
    public String getLastUpdated() {
        String lastUpdateStr =  measurements.get(measurements.size() - 1).getTimestamp().toString();
        return lastUpdateStr.substring(0, lastUpdateStr.length()-5);
    }

    public boolean getHasBeenConfigured() {
        return hasBeenConfigured;
    }

    public void setHasBeenConfigured(boolean hasBeenConfigured) {
        this.hasBeenConfigured = hasBeenConfigured;
    }

    public int getWeightIndicator() {
        return weightIndicator;
    }

    public void setWeightIndicator(int weightIndicator) {
        this.weightIndicator = weightIndicator;
    }

    public void resetStatuses() {
        this.illumStatus = Status.UNDEFINED;
        this.humidStatus = Status.OK;
        this.tempStatus = Status.OK;
        this.weightStatus = Status.OK;
    }

    public int getTempIndicator() {
        return tempIndicator;
    }

    public void setTempIndicator(int tempIndicator) {
        this.tempIndicator = tempIndicator;
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
        return new ArrayList<>(this.statusIntrospection);
    }

    public void setStatusIntrospection(List<StatusIntrospection> statusIntrospection) {
        this.statusIntrospection = statusIntrospection;
    }

    @Override
    public int compareTo(Hive o) {
        return this.name.compareTo(o.name);
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
    public static class StatusIntrospection implements Serializable {
        private Variables variable;
        private Status status;
        private DataAnalysis reasoning;

        public StatusIntrospection(Variables variable, Status status, DataAnalysis reasoning) {
            this.variable = variable;
            this.status = status;
            this.reasoning = reasoning;
        }

        public void setVariable(Variables variable) {
            this.variable = variable;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public void setReasoning(DataAnalysis reasoning) {
            this.reasoning = reasoning;
        }

        public Variables getVariable() {
            return variable;
        }

        public Status getStatus() {
            return status;
        }

        public DataAnalysis getReasoning() {
            return reasoning;
        }
    }

    public enum DataAnalysis {
        // The case where logic attempts to set 24 hours WEIGHT Delta
        CASE_DELTA_CALCULATIONS,
        // If UNDEFINED, then the logic was unable to get data to perform the correct calculations. Hive Delta is set to NaN
        // If OK, then the logic was able to calculate it and set it without any issue.

        // The case where a specific Variable falls below a specific threshold
        CASE_CRITICAL_THRESHOLD,
        // If the variable is WEIGHT
        //// If Danger, then the hive Variable fell below that threshold. Risk of starvation
        //// If OK, then the hive Variable

        // If the variable is TEMPERATURE
        //// If Warning, Temperature below configured value. Worst case: The queen is perhaps dead Normal case: The brood has simply moved away from the censor.
        //// If OK, no problem with the temperature.

        // The case where a variable as a function of time changes suddenly
        CASE_SUDDEN_CHANGE
        // If the variable is WEIGHT
        //// If Danger, Sudden change in hive weight: Due to it being summer it is probably swarming. Best case: Swarming. Worst case: Robbery from other bees
        //// If OK, then the weight change is nothing unexpected.
        //// If Warning, Sudden change in hive weight: Due to it not being summer it is probably a robbery. Best case: Swarming. Worst case: Robbery
    }

 /*   public void appendMeasurements(List<Measurement> ms){
        this.measurements.addAll(ms);
        measurements.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));
    }*/
 public static Hive createEmptyHive(int id, String name, long timeDelta){
     Hive h = new Hive(id, name);
     List<Measurement> m = new ArrayList<>();

     long now = System.currentTimeMillis();
     long startTime = now - timeDelta;
     long currTimestamp = startTime;
     while(currTimestamp < now){
         currTimestamp += 1000*60*5;
         m.add(new Measurement(new Timestamp(currTimestamp), 0,0,0,0));
     }
     h.setMeasurements(m);
     return h;
 }

}
