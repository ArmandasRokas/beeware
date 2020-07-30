package dk.dtu.group22.beeware.dal.dao.implementation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class CachedHiveRepoSQLImplTest {
    private CachedHiveRepoI repo;


    @Before
    public void setUp(){
        repo = new CachedHiveRepoSQLImpl(RuntimeEnvironment.application);
    }

    @Test
    public void givenHiveWithWeightToStore_returnHiveWithWeightFromDB(){
        // Arrange
        int id = 99998;
        String hiveName = "testHive1";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis()-1000*60*60);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        assertEquals(hiveName, returnedHive.getName());
        assertEquals(id, returnedHive.getId());
        assertEquals(weightKg, returnedHive.getMeasurements().get(0).getWeight(), 0.0);
    }

    @Test
    public void givenHiveWithTimestampToStore_returnHiveFromDB(){
        // Arrange
        int id = 99998;
        String hiveName = "testHive1";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        long time = System.currentTimeMillis()-1000*60*60;
        Timestamp timestamp = new Timestamp(time);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        assertEquals(hiveName, returnedHive.getName());
        assertEquals(id, returnedHive.getId());
        assertEquals(weightKg, returnedHive.getMeasurements().get(0).getWeight(), 0.0);
        assertEquals(time, returnedHive.getMeasurements().get(0).getTimestamp().getTime());
    }

    @Test
    public void givenHiveWithWeightAndIndicatorsToStore_returnHiveFromDB(){
        // Arrange
        int id = 99998;
        String hiveName = "testHive1";
        int weightIndicator = 20;
        int tempIndicator = 40;
        Hive hive = new Hive(id,hiveName);
        hive.setWeightIndicator(weightIndicator);
        hive.setTempIndicator(tempIndicator);

        List<Measurement> data_measure = new ArrayList<>();
        long time = System.currentTimeMillis()-1000*60*60;
        Timestamp timestamp = new Timestamp(time);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        assertEquals(hiveName, returnedHive.getName());
        assertEquals(id, returnedHive.getId());
        assertEquals(weightKg, returnedHive.getMeasurements().get(0).getWeight(), 0.0);
        assertEquals(weightIndicator, returnedHive.getWeightIndicator());
        assertEquals(tempIndicator, returnedHive.getTempIndicator());

    }

    @Test
    public void givenHiveWithMultipleWeightMeasurementsToStore_returnHiveWithWeightFromDB(){
        // Arrange
        int id = 99997;
        String hiveName = "testHive2";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis()-1000*60*10);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));

        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis()-1000*60*5);
        double weightKg2 = 32.0;
        double tempC2 = 20.0;
        double humidity2 = 80.0;
        double illuminance2 = 100.0;
        data_measure.add(new Measurement(timestamp2, weightKg2, tempC2, humidity2, illuminance2));

        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        List<Double> expected = new ArrayList<>();
        expected.add(weightKg);
        expected.add(weightKg2);
        Collections.sort(expected);

        List<Double> actual = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actual.add(m.getWeight());
        }

        assertEquals(expected, actual);
    }

    @Test
    public void givenHiveWithMultipleMeasurementsToStore_returnHiveWithWeightFromDB(){
        // Arrange
        int id = 99997;
        String hiveName = "testHive2";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis()-1000*60*10);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));

        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis()-1000*60*5);
        double weightKg2 = 32.0;
        double tempC2 = 22.0;
        double humidity2 = 82.0;
        double illuminance2 = 102.0;
        data_measure.add(new Measurement(timestamp2, weightKg2, tempC2, humidity2, illuminance2));

        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        List<Double> expectedWeight = new ArrayList<>();
        expectedWeight.add(weightKg);
        expectedWeight.add(weightKg2);
        Collections.sort(expectedWeight);

        List<Double> actualWeight = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actualWeight.add(m.getWeight());
        }

        List<Double> expectedTempC = new ArrayList<>();
        expectedTempC.add(tempC);
        expectedTempC.add(tempC2);
        Collections.sort(expectedTempC);

        List<Double> actualTempC = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actualTempC.add(m.getTempIn());
        }

        List<Double> expectedHum = new ArrayList<>();
        expectedHum.add(humidity);
        expectedHum.add(humidity2);
        Collections.sort(expectedHum);

        List<Double> actualHum = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actualHum.add(m.getHumidity());
        }

        List<Double> expectedIllum = new ArrayList<>();
        expectedIllum.add(illuminance);
        expectedIllum.add(illuminance2);
        Collections.sort(expectedIllum);

        List<Double> actualIllum = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actualIllum.add(m.getIlluminance());
        }



        assertEquals(expectedWeight, actualWeight);
        assertEquals(expectedTempC, actualTempC);
        assertEquals(expectedHum, actualHum);
        assertEquals(expectedIllum, actualIllum);
    }

    @Test
    public void givenMultipleHiveWithWeightMeasurementsToStore_ReturnOnlyRequiredHiveWithRightMeasurements(){
        // Arrange
        // Hive number 1
        int id1 = 1;
        String name1 = "name1";
        Hive hive1 = new Hive(id1,name1);

        List<Measurement> data_measure1 = new ArrayList<>();
        Timestamp timestamp1_1 = new Timestamp(System.currentTimeMillis()-1000*60*10);
        double weightKg1_1 = 29.0;
        double tempC1_1 = 20.0;
        double humidity1_1 = 80.0;
        double illuminance1_1 = 100.0;

        data_measure1.add(new Measurement(timestamp1_1, weightKg1_1, tempC1_1, humidity1_1, illuminance1_1));

        Timestamp timestamp1_2 = new Timestamp(System.currentTimeMillis()-1000*60*5);
        double weightKg1_2 = 21.0;
        double tempC1_2 = 20.0;
        double humidity1_2 = 80.0;
        double illuminance1_2 = 100.0;
        data_measure1.add(new Measurement(timestamp1_2, weightKg1_2, tempC1_2, humidity1_2, illuminance1_2));

        hive1.setMeasurements(data_measure1);

        // Hive number 2
        int id2 = 2;
        String name2 = "name2";
        Hive hive2 = new Hive(id2,name2);

        List<Measurement> data_measure2 = new ArrayList<>();
        Timestamp timestamp2_1 = new Timestamp(System.currentTimeMillis()-1000*60*10);
        double weightKg2_1 = 30.0;
        double tempC2_1 = 20.0;
        double humidity2_1 = 80.0;
        double illuminance2_1 = 100.0;

        data_measure2.add(new Measurement(timestamp2_1, weightKg2_1, tempC2_1, humidity2_1, illuminance2_1));

        Timestamp timestamp2_2 = new Timestamp(System.currentTimeMillis()-1000*60*5);
        double weightKg2_2 = 32.0;
        double tempC2_2 = 20.0;
        double humidity2_2 = 80.0;
        double illuminance2_2 = 100.0;
        data_measure2.add(new Measurement(timestamp2_2, weightKg2_2, tempC2_2, humidity2_2, illuminance2_2));

        hive2.setMeasurements(data_measure2);

        // Act
        repo.createCachedHive(hive1);
        repo.createCachedHive(hive2);

        Hive returnedHive1 = repo.getCachedHiveWithAllData(id1);

        List<Double> expected = new ArrayList<>();
        expected.add(weightKg1_1);
        expected.add(weightKg1_2);
        Collections.sort(expected);

        List<Double> actual = new ArrayList<>();
        for (Measurement m: returnedHive1.getMeasurements() ) {
            actual.add(m.getWeight());
        }
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

    /**
     Update:
    - save one Hive to SQLite
    - change indicator , add measurements
    - put a hive to updateHive
    - getHiveWithAll measurements
    - test if the received hive is updated correctly
     */
    @Test
    public void givenHiveToUpdate_returnUpdatedHive(){
        // Arrange
        int id = 99998;
        String hiveName = "testHive1";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis()-1000*60*60);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
        hive.setMeasurements(data_measure);


        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);
        // Add a new measurement
        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis()-1000*60*30);
        double weightKg2 = 32.0;
        double tempC2 = 22.0;
        double humidity2 = 82.0;
        double illuminance2 = 102.0;

        returnedHive.getMeasurements().add(new Measurement(timestamp2, weightKg2, tempC2, humidity2, illuminance2));

        // Change indicator
        int newTempIndicator = 99;
        int newWeightIndicator = 88;
        returnedHive.setTempIndicator(newTempIndicator);
        returnedHive.setWeightIndicator(newWeightIndicator);

        // Act
        repo.updateHive(returnedHive);// FIXME Should be updateHive
        Hive returnedUpdatedHive = repo.getCachedHiveWithAllData(id);
        // Assert

        List<Double> expectedWeight = new ArrayList<>();
        expectedWeight.add(weightKg);
        expectedWeight.add(weightKg2);
        Collections.sort(expectedWeight);

        List<Double> actualWeight = new ArrayList<>();
        for (Measurement m: returnedUpdatedHive.getMeasurements() ) {
            actualWeight.add(m.getWeight());
        }

        List<Double> expectedTempC = new ArrayList<>();
        expectedTempC.add(tempC);
        expectedTempC.add(tempC2);
        Collections.sort(expectedTempC);

        List<Double> actualTempC = new ArrayList<>();
        for (Measurement m: returnedUpdatedHive.getMeasurements() ) {
            actualTempC.add(m.getTempIn());
        }

        List<Double> expectedHum = new ArrayList<>();
        expectedHum.add(humidity);
        expectedHum.add(humidity2);
        Collections.sort(expectedHum);

        List<Double> actualHum = new ArrayList<>();
        for (Measurement m: returnedUpdatedHive.getMeasurements() ) {
            actualHum.add(m.getHumidity());
        }

        List<Double> expectedIllum = new ArrayList<>();
        expectedIllum.add(illuminance);
        expectedIllum.add(illuminance2);
        Collections.sort(expectedIllum);

        List<Double> actualIllum = new ArrayList<>();
        for (Measurement m: returnedUpdatedHive.getMeasurements() ) {
            actualIllum.add(m.getIlluminance());
        }

        assertEquals(expectedWeight, actualWeight);
        assertEquals(expectedTempC, actualTempC);
        assertEquals(expectedHum, actualHum);
        assertEquals(expectedIllum, actualIllum);
        assertEquals(newWeightIndicator, returnedUpdatedHive.getWeightIndicator());
        assertEquals(newTempIndicator, returnedUpdatedHive.getTempIndicator());
    }

    @Test
    public void givenTwoSameMeasurements_returnOnlyOne(){
        // Arrange
        int id = 99997;
        String hiveName = "testHive2";
        Hive hive = new Hive(id,hiveName);

        List<Measurement> data_measure = new ArrayList<>();
        long currTime = System.currentTimeMillis()-1000*60*10;
        Timestamp timestamp = new Timestamp(currTime);
        double weightKg = 30.0;
        double tempC = 20.0;
        double humidity = 80.0;
        double illuminance = 100.0;

        data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));

        Timestamp timestamp2 = new Timestamp(currTime);
        double weightKg2 = 30.0;
        double tempC2 = 20.0;
        double humidity2 = 80.0;
        double illuminance2 = 100.0;
        data_measure.add(new Measurement(timestamp, weightKg2, tempC2, humidity2, illuminance2));

        hive.setMeasurements(data_measure);

        // Act
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);

        // Assert
        List<Double> expected = new ArrayList<>();
        expected.add(weightKg);

        List<Double> actual = new ArrayList<>();
        for (Measurement m: returnedHive.getMeasurements() ) {
            actual.add(m.getWeight());
        }

        assertEquals(expected, actual);
    }
     @Test
    public void givenNotOrderedmeasurements_ReturnOrdered(){
         // Arrange
         int id = 99997;
         String hiveName = "testHive2";
         Hive hive = new Hive(id,hiveName);

         List<Measurement> data_measure = new ArrayList<>();
         long  currTime = System.currentTimeMillis();

         data_measure.add(new Measurement(new Timestamp(currTime-1000*60*10), 34.0, 24.0, 82, 102));
         data_measure.add(new Measurement(new Timestamp(currTime-1000*60*5), 30.0, 20.0, 80.0, 100.0));
         data_measure.add(new Measurement(new Timestamp(currTime-1000*60*15), 32.0, 22.0, 80, 100));

         hive.setMeasurements(data_measure);

         // Act
         repo.createCachedHive(hive);
         Hive returnedHive = repo.getCachedHiveWithAllData(id);

         // Assert
         Comparator<Measurement> comparator = (m1, m2) -> Long.compare(m1.getTimestamp().getTime(), m2.getTimestamp().getTime());
         Collections.sort(data_measure, comparator);
         assertEquals(data_measure.toString(), returnedHive.getMeasurements().toString());
     }

    /**
     * Next test. getHiveInterval:
     * Add 4 measurements and query only
     * two in the middle with sinceTime and untilTime
     */
}