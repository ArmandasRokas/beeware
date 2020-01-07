package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;

public class Overview extends AppCompatActivity implements View.OnClickListener {

    GridView gridView;
    private Logic logic;
    private ImageButton subHiveButton;
    private OverviewAdapter overviewAdapter;
    private TextView listEmptyTv;
    private ProgressBar progressBar;
    private Context ctx;
    private CachingManager cachingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Initialising variables
        ctx = this;
        logic = Logic.getSingleton();
        logic.setContext(ctx);
        // Boilerplate code to set Context to the CachingManager
        cachingManager = CachingManager.getSingleton();
        cachingManager.setCtx(ctx);

        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);
        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);

    }

    void setupSubscribedHives(boolean run) {
        AsyncTask asyncTask = new AsyncTask() {
            List<Hive> hives = new ArrayList<>();
            String errorMsg = null;
            @Override
            protected void onPreExecute() {
                listEmptyTv.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hives = logic.getSubscribedHives(2);
                    Collections.sort(hives);

                    return null;
                }
                catch (Exception e) {
                //    errorMsg = e.getMessage();
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(Object titler) {
                progressBar.setVisibility(View.INVISIBLE);
                if(cachingManager.isConnectionFailed()){
                    Toast toast = Toast.makeText(ctx, "Unable to fetch the newest data.\n Check your internet connection.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if(logic.getSubscriptionIDs().size() == 0){
                    listEmptyTv.setText("You have not subscribed to any hives.\nClick the edit button to subscribe to a hive.");
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
                } else if(hives.size() == 0){
                    listEmptyTv.setText("Error. Data could not be fetched.");
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
                } else {
                    listEmptyTv.setVisibility(View.INVISIBLE);
                    overviewAdapter = new OverviewAdapter(ctx, hives);
                    gridView.setAdapter(overviewAdapter);
                    gridView.setVisibility(View.VISIBLE);
                }
            }
        };

        if (run) {
            asyncTask.execute();
        } else {
            asyncTask.cancel(true);
        }

    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        setupSubscribedHives(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setupSubscribedHives(false);
    }

    @Override
    public void onClick(View view) {
        if (view == subHiveButton) {
            Intent ID = new Intent(this, SubscribeRecycler.class);
            startActivity(ID);
        }
    }
}
