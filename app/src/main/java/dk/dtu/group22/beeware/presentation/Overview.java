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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
                for (Hive hive : hives) {
                    if (!hive.getHasBeenConfigured()) {
                        ConfigPromptFragment cpf = new ConfigPromptFragment();
                        cpf.show(getSupportFragmentManager(), "ConfigurationPromptDialog");
                        break;
                    }
                }

                if (cachingManager.isConnectionFailed()) {
                    Toast toast = Toast.makeText(ctx, R.string.UnableToFetchNewestData, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (logic.getSubscriptionIDs().size() == 0) {
                    listEmptyTv.setText(R.string.YouHaveNotSubscribedToAnyHives);
                    listEmptyTv.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.INVISIBLE);
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
            }
        };

        if (run) {
            asyncTask.execute();
        } else {
            asyncTask.cancel(true);
        }
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        checkInternetConnection();
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

    // This code is from an answer on stackoverflow.com by Levite Dec 5 '14 - it checks whether device has an internet connection
    // Link to thread: https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out?page=1&tab=votes#tab-top
    public void checkInternetConnection() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    int timeoutMs = 1500;
                    Socket sock = new Socket();
                    SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

                    sock.connect(sockaddr, timeoutMs);
                    sock.close();

                    return "connected";
                } catch (IOException e) {
                    return "error";
                }
            }

            @Override
            protected void onPostExecute(String string) {
                if (string.equals("connected")) {
                    // do nothing
                } else if (string.equals("error")) {
                    Toast.makeText(ctx, getString(R.string.NoInternetConnection), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

}
