package dk.dtu.group22.beeware.presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dao.Hive;

public class TestingHiveTool extends AppCompatActivity {
    private ILogic logic = new Logic();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unused_testing_hive_tool);

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
            Hive hive = new Hive();
            String errorMsg = null;
            @Override
            protected void onPreExecute() {
                //progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hive.setId(240);
                    hive = logic.getHive(hive, since, until);
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
                if (errorMsg != null){
                    hive.toString();
                    //errorTv.setText(errorMsg);
                } else{
                //TODO call method that produces graph with hive.
                }
            }
        }.execute();

    }
    private void updateText(Hive hive){

    }
}