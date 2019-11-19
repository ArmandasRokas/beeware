package dk.dtu.group22.beeware.business.implementation;

import org.junit.Test;

import java.sql.Timestamp;

import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class LogicTest {

    @Test
    public void GivenGetHiveThreeTimes_ReturnSortedHive() throws InterruptedException {
        Logic logic = Logic.getSingleton();
        long now = System.currentTimeMillis();
        long since = now - (86400000 * 3);
        long beforeSince = since - (86400000 * 3);
        long beforeBeforeSince = beforeSince - (86400000 * 3);
        Hive hive = logic.getHive(240, new Timestamp(beforeSince), new Timestamp(since));

        Hive hive2 = logic.getHive(240, new Timestamp(beforeBeforeSince), new Timestamp(since));

        Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));
        Thread.sleep(300000 *2);
        now = System.currentTimeMillis();
        Hive hive4 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));

        boolean isSorted = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 1; i++){
            if(hive4.getMeasurements().get(i).getTimestamp().after(hive4.getMeasurements().get(i+1).getTimestamp())){
                isSorted = false;
                System.out.println(hive4.getMeasurements().get(i).getTimestamp() + " " +hive4.getMeasurements().get(i+1).getTimestamp() );
            }
        }
        for(Measurement m: hive4.getMeasurements()){
            System.out.println(m.getTimestamp());
        }
        assertTrue(isSorted);
    }
}