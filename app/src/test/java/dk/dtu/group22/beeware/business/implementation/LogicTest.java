package dk.dtu.group22.beeware.business.implementation;

import org.junit.Test;

import java.sql.Timestamp;

import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dto.implementation.HiveCached;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class LogicTest {

    @Test
    public void GivenGetHiveThreeTimes_SleepOneMinute_ReturnSortedHive() throws InterruptedException {
        Logic logic = Logic.getSingleton();
        long now = 1574237548640L;
        long fiveMinBeforeNow = (now - 300000 *2);
        long since = now - (86400000 * 3);
        long beforeSince = since - (86400000 * 3);
        long beforeBeforeSince = beforeSince - (86400000 * 3);
        Hive hive = logic.getHive(240, new Timestamp(beforeSince), new Timestamp(since));

        Hive hive2 = logic.getHive(240, new Timestamp(beforeBeforeSince), new Timestamp(since));

        //Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(fiveMinBeforeNow));
        now = System.currentTimeMillis();
        Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));
        Thread.sleep(300000 /5);
        now = System.currentTimeMillis();
        Hive hive4 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));

        boolean isSorted = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 1; i++){
            if(hive4.getMeasurements().get(i).getTimestamp().after(hive4.getMeasurements().get(i+1).getTimestamp())){
                isSorted = false;
                System.out.println(hive4.getMeasurements().get(i).getTimestamp() + " " +hive4.getMeasurements().get(i+1).getTimestamp() );
            }
        }
      /*  for(Measurement m: hive4.getMeasurements()){
            System.out.println(m.getTimestamp());
        }*/

        assertTrue(isSorted);
        // Test if the gaps between timestamps is maximum of length 10 min
        long tenMinInMillis = 10*60*1000;
        boolean noDeltaGT10Min = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 2; i++){
            long t1 = hive4.getMeasurements().get(i).getTimestamp().getTime();
            long t2 = hive4.getMeasurements().get(i+1).getTimestamp().getTime();
            if(t2-t1>tenMinInMillis){
                noDeltaGT10Min = false;
                break;
            }
        }
        assertTrue(noDeltaGT10Min);
        // Clean up
        HiveCached hiveCached = HiveCached.getSingleton();
        hiveCached.cleanCachedHives();
    }

    @Test
    public void GivenGetHiveThreeTimes_TenMinutes_ReturnSortedHive() throws InterruptedException {
        Logic logic = Logic.getSingleton();
        long now = 1574237548640L;
        long tenMinBeforeNow = (now - 300000 *2);
        long since = now - (86400000 * 3);
        long beforeSince = since - (86400000 * 3);
        long beforeBeforeSince = beforeSince - (86400000 * 3);
        Hive hive = logic.getHive(240, new Timestamp(beforeSince), new Timestamp(since));

        Hive hive2 = logic.getHive(240, new Timestamp(beforeBeforeSince), new Timestamp(since));

        Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(tenMinBeforeNow));
      //  now = System.currentTimeMillis();
       // Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));
        //Thread.sleep(300000 *2);
       // now = System.currentTimeMillis();
        Hive hive4 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));

        boolean isSorted = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 1; i++){
            if(hive4.getMeasurements().get(i).getTimestamp().after(hive4.getMeasurements().get(i+1).getTimestamp())){
                isSorted = false;
                System.out.println(hive4.getMeasurements().get(i).getTimestamp() + " " +hive4.getMeasurements().get(i+1).getTimestamp() );
            }
        }
        /*for(Measurement m: hive4.getMeasurements()){
            System.out.println(m.getTimestamp());
        }*/

        assertTrue(isSorted);
        // Test if the gaps between timestamps is maximum of length 10 min
        long tenMinInMillis = 10*60*1000;
        boolean noDeltaGT10Min = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 2; i++){
            long t1 = hive4.getMeasurements().get(i).getTimestamp().getTime();
            long t2 = hive4.getMeasurements().get(i+1).getTimestamp().getTime();
            if(t2-t1>tenMinInMillis){
                noDeltaGT10Min = false;
                break;
            }
        }
        assertTrue(noDeltaGT10Min);

        // Clean up
        HiveCached hiveCached = HiveCached.getSingleton();
        hiveCached.cleanCachedHives();
    }


    @Test
    public void GivenGetHive_ThreeMonthAgo_ReturnSortedHive() throws InterruptedException {
        Logic logic = Logic.getSingleton();
        long now = 1574237548640L;
        long threeMonthAgo = (now - 86400000 * 90L);
        long since = now - (86400000 * 3);
        Hive hive_since = logic.getHive(240, new Timestamp(since), new Timestamp(now));
        Hive hive = logic.getHive(240, new Timestamp(threeMonthAgo), new Timestamp(now));


        boolean isSorted = true;
        for(int i = 0; i < hive.getMeasurements().size() - 1; i++){
            if(hive.getMeasurements().get(i).getTimestamp().after(hive.getMeasurements().get(i+1).getTimestamp())){
                isSorted = false;
                System.out.println(hive.getMeasurements().get(i).getTimestamp() + " " +hive.getMeasurements().get(i+1).getTimestamp() );
            }
        }
    /*    for(Measurement m: hive.getMeasurements()){
            System.out.println(m.getTimestamp());
        }*/

        assertTrue(isSorted);
        // Test if the gaps between timestamps is maximum of length 10 min
        long tenMinInMillis = 60*1000*60*24*3;
        boolean noDeltaGT10Min = true;
        for(int i = 0; i < hive.getMeasurements().size() - 2; i++){
            long t1 = hive.getMeasurements().get(i).getTimestamp().getTime();
            long t2 = hive.getMeasurements().get(i+1).getTimestamp().getTime();
            if(t2-t1>tenMinInMillis){
                noDeltaGT10Min = false;
                System.out.println("Fail: " + hive.getMeasurements().get(i).getTimestamp() +  " "  + hive.getMeasurements().get(i+1).getTimestamp());
                System.out.println(t1 +" " + t2);
                //break;
            }
        }
        assertTrue(noDeltaGT10Min);

        // Clean up
        HiveCached hiveCached = HiveCached.getSingleton();
        hiveCached.cleanCachedHives();
    }


}