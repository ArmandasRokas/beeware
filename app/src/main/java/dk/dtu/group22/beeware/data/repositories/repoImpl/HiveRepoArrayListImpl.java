package dk.dtu.group22.beeware.data.repositories.repoImpl;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.Measurement;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

public class HiveRepoArrayListImpl implements HiveRepository {

    private static List<Hive> hiveList = new ArrayList<>();
    private static final int MINUTE_SUBTRACTION = 60000;
    static final long CURR_TIME = 1570195921501L;

//    static {
//        // dummy hives 102
//        Measurement meas1 = new Measurement();
//        meas1.setTimestamp(new Timestamp(CURR_TIME));
//        meas1.setWeight(32.0);
//        meas1.setTempIn(35.0);
//        meas1.setHumidity(98.9);
//        meas1.setIlluminance(50000);
//
//        Measurement meas2 = new Measurement();
//        meas2.setTimestamp(new Timestamp(CURR_TIME - MINUTE_SUBTRACTION));
//        meas2.setWeight(31.9);
//        meas2.setTempIn(35.1);
//        meas2.setHumidity(99.0);
//        meas2.setIlluminance(49900);
//
//        List<Measurement> measurements = new ArrayList<>();
//        measurements.add(meas1);
//        measurements.add(meas2);
//
//        Hive test1 = new Hive();
//        test1.setId(102);
//        test1.setName("FHA_Stade102");
//        test1.setMeasurements(measurements);
//        hiveList.add(test1);
//
//        // dummy hive 103
//        Measurement meas103_1 = new Measurement();
//        meas103_1.setTimestamp(new Timestamp(CURR_TIME));
//        meas103_1.setWeight(33.0);
//        meas103_1.setTempIn(32.4);
//        meas103_1.setHumidity(99.4);
//        meas103_1.setIlluminance(45000);
//
//        Measurement meas103_2 = new Measurement();
//        meas103_2.setTimestamp(new Timestamp(CURR_TIME - MINUTE_SUBTRACTION));
//        meas103_2.setWeight(29.9);
//        meas103_2.setTempIn(36.1);
//        meas103_2.setHumidity(99.0);
//        meas103_2.setIlluminance(49900);
//
//        List<Measurement> measurements_103 = new ArrayList<>();
//        measurements_103.add(meas103_1);
//        measurements_103.add(meas103_2);
//
//        Hive test103 = new Hive();
//        test103.setId(103);
//        test103.setName("FHA_Stade103");
//        test103.setMeasurements(measurements_103);
//        hiveList.add(test103);
//
//        // dummy hive 104
//        Measurement meas104_1 = new Measurement();
//        meas104_1.setTimestamp(new Timestamp(CURR_TIME));
//        meas104_1.setWeight(33.0);
//        meas104_1.setTempIn(32.4);
//        meas104_1.setHumidity(99.4);
//        meas104_1.setIlluminance(45000);
//
//        Measurement meas104_2 = new Measurement();
//        meas104_2.setTimestamp(new Timestamp(CURR_TIME - MINUTE_SUBTRACTION));
//        meas104_2.setWeight(29.9);
//        meas104_2.setTempIn(36.1);
//        meas104_2.setHumidity(99.0);
//        meas104_2.setIlluminance(49900);
//
//        List<Measurement> measurements_104 = new ArrayList<>();
//        measurements_104.add(meas104_1);
//        measurements_104.add(meas104_2);
//
//        Hive hive104 = new Hive();
//        hive104.setId(104);
//        hive104.setName("FHA_Stade104");
//        hive104.setMeasurements(measurements_104);
//        hiveList.add(hive104);
//
//    }



    // TODO : implement timestams og throws. Armandas
    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        for (int i = 0; i < hiveList.size(); i++) {
            if(hive.getId() == hiveList.get(i).getId()){
                return hiveList.get(i);
            }
        }
        return null;
    }
}
