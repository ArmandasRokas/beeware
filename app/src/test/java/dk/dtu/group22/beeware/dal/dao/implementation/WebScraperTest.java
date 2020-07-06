package dk.dtu.group22.beeware.dal.dao.implementation;

import org.junit.Test;

import java.io.IOException;
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class WebScraperTest {

    @Test
    public void isDataAvailableOnHiveTool_givenHiveWithNoDataOnHiveTool_returnFalse(){

        WebScraper w  = new WebScraper();
        long firstMarch = 1583017200000L;
        long firstApril = 1588284000000L;

        boolean actual = false;
        try {
            actual = w.isDataAvailableOnHiveTool(new Timestamp(firstMarch),new Timestamp(firstApril), 236);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertFalse(actual);
    }
    @Test
    public void isDataAvailableOnHiveTool_givenHiveWithDataOnHiveTool_returnTrue(){

        WebScraper w  = new WebScraper();
        long firstJuly = 1593554400000L;
        long thirdJuly = 1593727200000L;

        boolean actual = true;
        try {
            actual = w.isDataAvailableOnHiveTool(new Timestamp(firstJuly),new Timestamp(thirdJuly), 236);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(actual);
    }
}