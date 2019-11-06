package dk.dtu.group22.beeware.business.implementation;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dao.User;
import dk.dtu.group22.beeware.dal.dto.implementation.UserArraylist;

import static org.junit.Assert.*;

public class LogicTest {
    final long CURR_TIME = 1570195921501L;
    @Test
    public void givenUser_returnHiveSubscribed(){

        Logic hiveBusiness = new Logic();
        UserArraylist userRepo = (UserArraylist) hiveBusiness.getUserArraylist();
        userRepo.cleanSubscribedHives();
        User user = new User();
        user.setId(1);

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

        Hive hive = new Hive();
        hive.setId(102);
        hive.setName("FHA_Stade102");
        hive.setMeasurements(measurements);


        hiveBusiness.subscribeHive(user, hive);
        List<Hive> hivesExpected = new ArrayList<>();
        hivesExpected.add(hive);
        // Act
        List<Hive> returnedHives = hiveBusiness.getHives(user, 1);
        // Assert

        assertEquals(hivesExpected.toString(), returnedHives.toString());

        // new Timestamp(0), new Timestamp(CURR_TIME+100000)
    }
    @Test
    public void givenHiveWithId_returnHiveWithMeasurements(){
        ILogic hiveBusiness = new Logic();

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

        Hive hive = new Hive();
        hive.setId(102);
        hive.setName("FHA_Stade102");
        hive.setMeasurements(measurements);

        Hive returnedHive = hiveBusiness.getHive(hive, new Timestamp(0),new Timestamp(CURR_TIME));
        assertEquals(hive.toString(), returnedHive.toString());
    }

}