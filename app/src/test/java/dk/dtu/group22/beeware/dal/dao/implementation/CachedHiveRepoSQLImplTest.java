package dk.dtu.group22.beeware.dal.dao.implementation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.UUID;

import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class CachedHiveRepoSQLImplTest {
    private CachedHiveRepoI repo;


    @Before
    public void setUp(){
        repo = new CachedHiveRepoSQLImpl(RuntimeEnvironment.application);
    }

    @Test
    public void givenHiveWithIdAndName_ReturnHiveWithIdAndName() {
        int id = 999999;
        String hiveName = "testHive";
        Hive hive = new Hive(id, hiveName);
        repo.createCachedHive(hive);
        Hive returnedHive = repo.getCachedHiveWithAllData(id);
        assertEquals(hiveName, returnedHive.getName());
        assertEquals(id, returnedHive.getId());

    }
}