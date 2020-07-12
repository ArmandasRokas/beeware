package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.CustomActivity;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class Overview extends CustomActivity //implements View.OnClickListener
 {
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
    private SharedPreferences sharedPref;
    private Toast toastLatest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        toastLatest = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);

        // Initialising variables
        ctx = this;
        logic = Logic.getSingleton();
        logic.setContext(ctx);
        sharedPref = ctx.getSharedPreferences("pref", Context.MODE_PRIVATE);

        // Boilerplate code to set Context to the CachingManager
        cachingManager = CachingManager.getSingleton();
        cachingManager.setCtx(ctx);

        // Finding views
        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);
        subHiveButton = findViewById(R.id.subHiveBtn);
    //    subHiveButton.setOnClickListener(this);

        float subHiveButtonX = sharedPref.getFloat("fabX",0);
        float subHiveButtonY = sharedPref.getFloat("fabY",0);
        // Check if the position of subHiveButton is saved in SharedPreferences.
        if(subHiveButtonX != 0 || subHiveButtonY != 0){
            // Set the position of subHiveButton with values saved in SharedPreferences
            subHiveButton.setX(subHiveButtonX);
            subHiveButton.setY(subHiveButtonY);
        } else{
            // Put subHiveButton on the right bottom corner programmatically
            ConstraintLayout constraintLayout = findViewById(R.id.overviewConstrainLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.subHiveBtn,ConstraintSet.BOTTOM,R.id.overviewConstrainLayout,ConstraintSet.BOTTOM,24);
            constraintSet.connect(R.id.subHiveBtn,ConstraintSet.END,R.id.overviewConstrainLayout,ConstraintSet.END,24);
            constraintSet.applyTo(constraintLayout);
        }
        // Implements drag-and-drop subHiveButton
        subHiveButton.setOnTouchListener(new View.OnTouchListener() {

            float startX;
            float startRawX;
            float startRawY;
            float distanceX;
            float distanceY;
            int lastAction;
            float startY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = view.getX() - event.getRawX();
                        startY = view.getY() - event.getRawY();
                        startRawX = event.getRawX();
                        startRawY = event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float currX = event.getRawX()+ startX;
                        float currY = event.getRawY()  + startY;
                        view.setX(currX);
                        view.setY(currY);
                        lastAction = MotionEvent.ACTION_MOVE;
                        sharedPref.edit().putFloat("fabX", currX).commit();
                        sharedPref.edit().putFloat("fabY", currY).commit();
                        break;
                    case MotionEvent.ACTION_UP:
                        distanceX = event.getRawX()-startRawX;
                        distanceY = event.getRawY()-startRawY;
                        if (Math.abs(distanceX)< 10 && Math.abs(distanceY)<10){
                            Intent ID = new Intent(ctx, Subscribe.class);
                            startActivity(ID);
                        }
                        break;
                    case MotionEvent.ACTION_BUTTON_PRESS:
                    default:
                        return false;
                }
                return true;
            }
        });
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

           /*     if (cachingManager.isConnectionFailed()) { // Always true????
                    Toast toast = Toast.makeText(ctx, R.string.UnableToFetchNewestData, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }*/
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
                    Collections.sort(notFetchedHives);
                    String errMessage = getString(R.string.FailedToGetLatestData) +  " " + notFetchedHives.toString();
                    //Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
                    if(getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
                        toastLatest.setText(errMessage);
                        toastLatest.show();
                    }
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
        toastLatest.cancel();
    }

//    @Override
//    public void onClick(View view) {
//        if (view == subHiveButton) {
//            Intent ID = new Intent(this, Subscribe.class);
//            startActivity(ID);
//        }
//    }

    public void setConfigureNow(boolean configureNow) {
        this.configureNow = configureNow;
    }

}
