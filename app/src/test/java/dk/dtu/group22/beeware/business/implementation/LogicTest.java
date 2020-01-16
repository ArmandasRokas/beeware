package dk.dtu.group22.beeware.business.implementation;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
        CachingManager cachingManager = CachingManager.getSingleton();
        cachingManager.cleanCachedHives();
    }

    @Test
    public void GivenGetHiveThreeTimes_TenMinutes_ReturnSortedHive() throws InterruptedException {
        Logic logic = Logic.getSingleton();
        long tenMinInMillis = 10*60*1000;
        long now = 1574237548640L;
        long tenMinBeforeNow = (now - 300000 *2);
        long since = now - (86400000 * 3);
        long beforeSince = since - (86400000 * 3);
        long beforeBeforeSince = beforeSince - (86400000 * 3);
        Hive hive = logic.getHive(240, new Timestamp(beforeSince), new Timestamp(since));

        Hive hive2 = logic.getHive(240, new Timestamp(beforeBeforeSince), new Timestamp(since));

        Hive hive3 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(tenMinBeforeNow));
        Hive hive4 = logic.getHive(240,new Timestamp(beforeSince), new Timestamp(now));

        /**
         * Test if the measurements is sorted.
         * Test fails if duplicate measurements exists.
         */
        boolean isSorted = true;
        for(int i = 0; i < hive4.getMeasurements().size() - 1; i++){
            if(hive4.getMeasurements().get(i).getTimestamp().after(hive4.getMeasurements().get(i+1).getTimestamp())){
                isSorted = false;
                System.out.println(hive4.getMeasurements().get(i).getTimestamp() + " " +hive4.getMeasurements().get(i+1).getTimestamp() );
            }
        }
        assertTrue(isSorted);

        /**
         * Test if the gaps between timestamps is maximum of length 10 min
         */

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

        /**
         * Test if requested measurements is fetched
         */
        assertTrue( hive4.getMeasurements().get(0).getTimestamp().getTime() - beforeBeforeSince < tenMinInMillis);
        assertTrue(now - hive4.getMeasurements().get(hive4.getMeasurements().size()-1).getTimestamp().getTime() < tenMinInMillis);
        // Clean up
        CachingManager cachingManager = CachingManager.getSingleton();
        cachingManager.cleanCachedHives();
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
        CachingManager cachingManager = CachingManager.getSingleton();
        cachingManager.cleanCachedHives();
    }

    public void testGetHive(){

        // Testing, if "getHive()" will use its methods correctly, and return a full hive with
        // initialized values.
        Logic logic = Logic.getSingleton();

        long now = System.currentTimeMillis();

        Hive hive = logic.getHive(240, new Timestamp(now - 300000), new Timestamp(now));

        assertTrue(hive.getName().equals("FHA_Stade1"));
        assertFalse(hive.getMeasurements() == null);

    }

    public void testSubscribeHive(){
        Logic logic = Logic.getSingleton();

        // Test, that if i subscribe one hive with id = 240. then i only get that one subscribtion.
        logic.subscribeHive(240);
        assertEquals(logic.getSubscriptionIDs() , Arrays.asList(240));

        // Test that there are only 1 subscribed hive, and that it is the correct one (id 240 is FHA_Stade1)
        // and, that it has measurements.
        List<Hive> hives = logic.getSubscribedHives(1);
        assertTrue(hives.size() == 1);
        assertTrue(hives.get(0).getName() == "FHA_Stade1");
        assertFalse(hives.get(0).getMeasurements() == null);

        // Test, that if i now unsubscribe the hive, the list of subscribed hives and subscribed ids are now empty.
        logic.unsubscribeHive(240);
        List<Integer> emptyIDs = new ArrayList<>();
        List<Hive> emptyHives = new ArrayList<>();
        assertEquals(logic.getSubscriptionIDs(), emptyIDs);
        assertEquals(logic.getSubscribedHives(1), emptyHives);

    }
    @Test
    public void testgetNamesAndIDs(){
        Logic logic = Logic.getSingleton();

        // getNamesAndIDs() is called, in order to recieve a list of names and ids from Hivetool.net
        // Firstly, we test whether the list contains several active hives.
        List<NameIdPair> namesAndIDs = logic.getNamesAndIDs();
        NameIdPair FHAStade1 = new NameIdPair("FHA_Stade1", 240,true, "KBH");
        NameIdPair Athens = new NameIdPair("Athens", 10,true, "JAPAN");
        NameIdPair Colony1 = new NameIdPair("Colony1", 176,true, "STOCKHOLM");
        NameIdPair PetesBees = new NameIdPair("PetesBees", 112, true, "OSLO");

        assertTrue(namesAndIDs.contains(FHAStade1));
        assertTrue(namesAndIDs.contains(Athens));
        assertTrue(namesAndIDs.contains(Colony1));
        assertTrue(namesAndIDs.contains(PetesBees));

        // Secondly, we test to see if the list also contains inactive hives.

        NameIdPair Deseret = new NameIdPair("Deseret", 147,false, "MASSACHSUETS");
        NameIdPair England = new NameIdPair("England", 128,false, "TEXLCOCO");
        NameIdPair Judge = new NameIdPair("Judge", 101,false, "PRAG");
        NameIdPair Radiance = new NameIdPair("Radiance", 38,false, "BERGEN");

        assertTrue(namesAndIDs.contains(Deseret));
        assertTrue(namesAndIDs.contains(England));
        assertTrue(namesAndIDs.contains(Judge));
        assertTrue(namesAndIDs.contains(Radiance));


    }


}
