package dk.dtu.group22.beeware.presentation;

import android.os.AsyncTask;
import android.os.Bundle;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.CustomActivity;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class TesterHiveTool extends CustomActivity {
    private Logic logic = Logic.getSingleton();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateSince = null;
        Date dateUntil = null;
        try {
            dateSince = dateFormat.parse("20/10/2019");
            dateUntil = dateFormat.parse("27/10/2019");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timeSince = dateSince.getTime();
        long timeUntil = dateUntil.getTime();
        Timestamp since = new Timestamp(timeSince);
        Timestamp until = new Timestamp(timeUntil);


        new AsyncTask() {
            String errorMsg = null;
            Hive hive;

            @Override
            protected void onPreExecute() {
                //progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hive = logic.getHiveNetwork(240, since, until);
                    return null;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object titler) {
                System.out.println(hive);
                //      progressBar.setVisibility(View.INVISIBLE);
                if (errorMsg != null) {
                    hive.toString();
                    //errorTv.setText(errorMsg);
                } else {
                    //TODO call method that produces graph with hive.
                }
            }
        }.execute();

    }

    private void updateText(Hive hive) {
    }

}
