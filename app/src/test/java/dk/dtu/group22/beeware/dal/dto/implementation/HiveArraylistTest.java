package dk.dtu.group22.beeware.dal.dto.implementation;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;

import static org.junit.Assert.*;

public class HiveArraylistTest {



    @Test
    public void givenHiveWithId_returnHiveWithMeasurements(){
        // Arrange
        HiveArraylist hr = new HiveArraylist();
        Hive hive = new Hive();
        hive.setId(102);
        final long CURR_TIME = 1570195921501L;
        Measurement meas1 = new Measurement();
        meas1.setTimestamp(new Timestamp(CURR_TIME));
        meas1.setWeight(32.0);
        meas1.setTempIn(35.0);
        meas1.setHumidity(98.9);
        meas1.setIlluminance(50000);

        Measurement meas2 = new Measurement();
        meas2.setTimestamp(new Timestamp(CURR_TIME -60000L));
        meas2.setWeight(31.9);
        meas2.setTempIn(35.1);
        meas2.setHumidity(99.0);
        meas2.setIlluminance(49900);

        List<Measurement> measurements = new ArrayList<>();
        measurements.add(meas1);
        measurements.add(meas2);

        Hive test1 = new Hive();
        test1.setId(102);
        test1.setName("FHA_Stade102");
        test1.setMeasurements(measurements);


        // Act
        Hive hive_returned = hr.getHive(hive, new Timestamp(0) ,new Timestamp(System.currentTimeMillis()+60000));
        // Assert
        System.out.println(System.currentTimeMillis());
        assertEquals(test1.toString(), hive_returned.toString());
    }



}