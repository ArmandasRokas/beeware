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

        // Next test could be with multiple hives
}