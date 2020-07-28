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
}