package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationBroadcaster extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Recieved something", Toast.LENGTH_LONG).show();
    }

}
