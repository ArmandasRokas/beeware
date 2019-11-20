package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.Hive;

public class Overview extends AppCompatActivity implements View.OnClickListener {

    GridView gridView;
    private Logic logic;
    private ImageButton subHiveButton;
    private ImageAdapter imageAdapter;
    private TextView listEmptyTv;
    private ProgressBar progressBar;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Initialising variables
        ctx = this;
        logic = Logic.getSingleton();
        logic.setContext(ctx);

        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);
        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);

    }

    void setupSubscribedHives(boolean run) {
        AsyncTask asyncTask = new AsyncTask() {
            List<Hive> hives;
            String errorMsg = null;
            @Override
            protected void onPreExecute() {
                listEmptyTv.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hives = logic.getSubscribedHives(1);
                    return null;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object titler) {
                progressBar.setVisibility(View.INVISIBLE);
                if (errorMsg != null){
                  //  errorTv.setText(errorMsg);
                } else{
                    imageAdapter = new ImageAdapter(ctx, hives);
                    gridView.setAdapter(imageAdapter);
                    if (hives.size() == 0){
                        listEmptyTv.setVisibility(View.VISIBLE);
                    } else {
                        listEmptyTv.setVisibility(View.INVISIBLE);
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
}
