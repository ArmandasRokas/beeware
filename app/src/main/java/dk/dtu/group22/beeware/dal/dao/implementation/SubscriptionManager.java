package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscriptionManager;

public class SubscriptionManager implements ISubscriptionManager {
    private SharedPreferences sharedPreferences;
    private Context ctx;

    public SubscriptionManager(Context ctx) {
        this.ctx = ctx;
        sharedPreferences = ctx.getSharedPreferences(String.valueOf(R.string.subscriptions), Context.MODE_PRIVATE);
    }

    public void saveSubscription(int id) {

        String subscriptions = sharedPreferences.getString("ids", "");

        if (subscriptions.contains(String.valueOf(id))) {
            return;
        }

        if (subscriptions.equals("")) {
            subscriptions = id + ",";
        } else {
            subscriptions += id + ",";
        }
        sharedPreferences.edit().putString("ids", subscriptions).apply();
    }

    public List<Integer> getSubscriptions() {

        List<Integer> subscriptions_list = new ArrayList<>();
        String[] subscriptions_array = sharedPreferences.getString("ids", "").split(",");

        for (String id : subscriptions_array) {
            if (!id.equals("")) {
                subscriptions_list.add(Integer.valueOf(id));
            }
        }

        return subscriptions_list;
    }

    public void deleteSubscription(int id) {

        String[] subscriptions_before = sharedPreferences.getString("ids", "").split(",");
        for (int i = 0; i < subscriptions_before.length; i++) {
            int sub = Integer.valueOf(subscriptions_before[i]);
            if (sub == id) {
                subscriptions_before[i] = null;
            }
        }

        StringBuilder subscriptions_after = new StringBuilder();
        for (String s : subscriptions_before) {
            if (s != null) {
                subscriptions_after.append(s);
                subscriptions_after.append(",");
            }
        }

        sharedPreferences.edit().putString("ids", subscriptions_after.toString()).apply();
    }

    public void cacheHivesToSub(List<NameIdPair> hivesIdName) {
        SharedPreferences hiveNames = ctx.getSharedPreferences(String.valueOf("hiveNames"), Context.MODE_PRIVATE);
        SharedPreferences hiveLocation = ctx.getSharedPreferences(String.valueOf("hiveLocation"), Context.MODE_PRIVATE);
        for(NameIdPair e: hivesIdName){
            hiveNames.edit().putString(e.getID()+"", e.getName()).apply();
            hiveLocation.edit().putString(e.getID()+"", e.getLocation()).apply();
        }
    }
/*
    public String getCachedHiveName(int hiveId) {
            String hiveName = sharedPreferences.getString(hiveId+"", "");
            if(!hiveName.isEmpty()){
                return hiveName;
            } else {
                return "Unknown hive";
            }
    }
 */
    public NameIdPair getCachedNameIdPair(int hiveId){
        SharedPreferences hiveNames = ctx.getSharedPreferences(String.valueOf("hiveNames"), Context.MODE_PRIVATE);
        SharedPreferences hiveLocation = ctx.getSharedPreferences(String.valueOf("hiveLocation"), Context.MODE_PRIVATE);
        String hiveName = hiveNames.getString(hiveId+"", "");
        String location = hiveLocation.getString(hiveId+"", "");
        if(!hiveName.isEmpty() && !location.isEmpty()){
            return new NameIdPair(hiveName, hiveId, true, location);
        } else {
            return new NameIdPair("Unknown hive", hiveId, true, "Unknown location");
        }
    }
}
