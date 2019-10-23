package dk.dtu.group22.beeware.data.repositories.repoImpl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.UserRepository;

import static org.junit.Assert.*;

public class UserRepoArrayListImplTest {

    @Test
    public void givenUserAndHiveToSubsribe_returnSubsribedUserHivesContainsGivenHive() {
        // Arrange
        UserRepoArrayListImpl hr = new UserRepoArrayListImpl();
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

    @Test
    public void givenUserAndHiveToUnsubscribe_returnMissingSubscription() {
        // Arrange
        UserRepoArrayListImpl hr = new UserRepoArrayListImpl();
        hr.cleanSubscribedHives();
        User user = new User();
        user.setId(1);
        Hive hive = new Hive();
        hive.setId(101);
        hr.subscribeHive(user, hive);

        // Act
        hr.unsubscribeHive(user, hive);
        List<Hive> hives = hr.getSubscribedHives(user);

        // Assert
        List<Hive> test = new ArrayList<>();
        assertEquals(test, hives);
    }

}