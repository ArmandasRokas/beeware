package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;

public class TestingHiveTool extends AppCompatActivity {
    private HiveBusiness hiveBusiness = new HiveBusinessImpl();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_hive_tool);

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
                    hive = hiveBusiness.getHive(hive, since, until);
                    return null;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object titler) {
          //      progressBar.setVisibility(View.INVISIBLE);
                if (errorMsg != null){
                    //errorTv.setText(errorMsg);
                } else{
                //TODO call method that produces graph with hive.
                }
            }
        }.execute();

    }
}
