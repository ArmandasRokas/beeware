package dk.dtu.group22.beeware.presentation;

import android.app.NotificationManager;
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
import java.util.Collections;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dao.implementation.CachingManager;

import static androidx.core.content.ContextCompat.getSystemService;

public class Overview extends AppCompatActivity implements View.OnClickListener {

    GridView gridView;
    private Logic logic;
    private ImageButton subHiveButton;
    private OverviewAdapter overviewAdapter;
    private TextView listEmptyTv;
    private ProgressBar progressBar;
    private Context ctx;
    private CachingManager cachingManager;
    private Overview overview;
    private ArrayList<Integer> subscribedHiveIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);


        // Initialising variables
        ctx = this;
        logic = Logic.getSingleton();
        logic.setContext(ctx);
        //TODO Insert in App-class.
        logic.setNotificationManager((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        // Boilerplate code to set Context to the CachingManager
        cachingManager = CachingManager.getSingleton();
        cachingManager.setCtx(ctx);

        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);
        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);



    }

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
                    for(Integer i: logic.getSubscriptionIDs()){

                        subscribedHiveIDs.add(i);

                    }
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
                    Toast toast = Toast.makeText(ctx, R.string.UnableToFetchNewestData, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if(logic.getSubscriptionIDs().size() == 0){
                    listEmptyTv.setText(R.string.YouHaveNotSubscribedToAnyHives);
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
                } else if(hives.size() == 0){
                    listEmptyTv.setText(R.string.ErrorDataCouldNotBeFetched);
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
                } else {
                    listEmptyTv.setVisibility(View.INVISIBLE);
                    overviewAdapter = new OverviewAdapter(ctx, hives);
                    gridView.setAdapter(overviewAdapter);
                    gridView.setVisibility(View.VISIBLE);
                }

                for(Hive hive: hives){
                    //Updates the icons of the hives, according to each hives' Configuration values.
                    logic.calculateHiveStatus(hive);
                }

                for(Hive hive: hives){
                    if(!hive.getHasBeenConfigured() ){
                        OnSubscriptionConfigurationFragment oscf = new OnSubscriptionConfigurationFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("ID", hive.getId());
                        oscf.setArguments(bundle);
                        oscf.show(getSupportFragmentManager(), "configurationDialog");
                    }
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

    public Overview getOverview() {
        return overview;
    }
}
