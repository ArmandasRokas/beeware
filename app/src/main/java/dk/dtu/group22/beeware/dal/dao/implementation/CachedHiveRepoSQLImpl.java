package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.Context;

import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class CachedHiveRepoSQLImpl implements CachedHiveRepoI {

    private Context ctx;

    public CachedHiveRepoSQLImpl(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public Hive getCachedHiveWithAllData(int hiveId) {
        return null;
    }

    @Override
    public void createCachedHive(Hive hive) {

    }
}
