package dk.dtu.group22.beeware.business.interfaceBusiness;

import dk.dtu.group22.beeware.data.entities.Hive;

public interface HiveBusiness {

    /**
     *
     * @param id
     * @param daysDelta number of days soulde be used to calculate a delta
     * @return A hive with the latest weight and weight delta
     */
    Hive getHive(int id, int daysDelta);

}
