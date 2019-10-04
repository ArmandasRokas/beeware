package dk.dtu.group22.beeware.data.repositories.repoImpl;

import org.junit.Test;

import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

import static org.junit.Assert.*;

public class HiveRepoArrayListImplTest {



    @Test
    public void givenUserAndHiveToSubsribe_returnSubsribedUserHivesContainsGivenHive(){
        // Arrange
        HiveRepoArrayListImpl hr = new HiveRepoArrayListImpl();
        hr.cleanSubscribedHives();
        User user = new User();
        user.setId(1);
        Hive hive = new Hive();
        hive.setId(101);
        // Act
        hr.subscribeHive(user, hive);
        List<Hive> hives = hr.getSubscribedHives(user);
        // Assert
        assertEquals(hive.getId(), hives.get(0).getId());
    }

}