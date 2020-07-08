package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.CustomActivity;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class Overview extends CustomActivity implements View.OnClickListener {
    GridView gridView;
    private Logic logic;
    private ImageButton subHiveButton;
    private OverviewAdapter overviewAdapter;
    private TextView listEmptyTv;
    private ProgressBar progressBar;
    private Context ctx;
    private CachingManager cachingManager;
    private boolean configureNow = false;
    private ArrayList<Integer> subscribedHiveIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Initialising variables
        ctx = this;
        logic = Logic.getSingleton();
        logic.setContext(ctx);

        // Boilerplate code to set Context to the CachingManager
        cachingManager = CachingManager.getSingleton();
        cachingManager.setCtx(ctx);

        // Finding views
        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);
        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);
    }

    /***
     * Sets up the overviews list of hives
     * @param run
     * A boolean of whether to run the async task or cancel it
     */
    // Shouldnt do-in-background check if it is cancelled?
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
                    for (Integer i : logic.getSubscriptionIDs()) {

                        subscribedHiveIDs.add(i);

                    }
                    return null;
                } catch (Exception e) {
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
                for (Hive hive : hives) {
                    System.out.println(hive.getName() + ": " + hive.getHasBeenConfigured());
                }

                progressBar.setVisibility(View.INVISIBLE);

                if (cachingManager.isConnectionFailed()) {
                    Toast toast = Toast.makeText(ctx, R.string.UnableToFetchNewestData, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (logic.getSubscriptionIDs().size() == 0) {
                    listEmptyTv.setText(R.string.YouHaveNotSubscribedToAnyHives);
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);

                    // Adds flashing effect to the edit button
                    final Animation animation = new AlphaAnimation(1, 0.2f); // Change alpha from fully visible to invisible
                    animation.setDuration(500); // duration - half a second
                    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                    animation.setRepeatCount(8);
                    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
                    subHiveButton.startAnimation(animation);
                } else if (hives.size() == 0) {
                    listEmptyTv.setText(R.string.ErrorDataCouldNotBeFetched);
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
                } else {
                    listEmptyTv.setVisibility(View.INVISIBLE);
                    overviewAdapter = new OverviewAdapter(ctx, hives);
                    gridView.setAdapter(overviewAdapter);
                    gridView.setVisibility(View.VISIBLE);
                }

                for (Hive hive : hives) {
                    //Updates the icons of the hives, according to each hives' Configuration values.
                    logic.calculateHiveStatus(hive);
                }
                List<String> notFetchedHives = logic.getNotFetchedHives();
                if(!notFetchedHives.isEmpty()){
                    System.out.println(notFetchedHives.toString());
                    String errMessage = getString(R.string.FailedToGetHive) +  " " + notFetchedHives.toString();
                    Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
                    logic.clearNotFetchHives();
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
    public void onResume() {
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
            Intent ID = new Intent(this, Subscribe.class);
            startActivity(ID);
        }
    }

    public void setConfigureNow(boolean configureNow) {
        this.configureNow = configureNow;
    }

}
