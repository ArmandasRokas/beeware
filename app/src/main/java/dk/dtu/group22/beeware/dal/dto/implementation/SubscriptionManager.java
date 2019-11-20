package dk.dtu.group22.beeware.dal.dto.implementation;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscriptionManager;

public class SubscriptionManager implements ISubscriptionManager {

    private SharedPreferences sharedPreferences;

    public SubscriptionManager(Context ctx) {
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

}
