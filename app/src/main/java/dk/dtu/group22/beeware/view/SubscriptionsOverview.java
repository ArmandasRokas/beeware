package dk.dtu.group22.beeware.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public class SubscriptionsOverview extends AppCompatActivity implements View.OnClickListener {

    GridView gridView;
    private static final int CONTENT_VIEW_ID = 10101010;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HiveBusiness hiveBusiness;
    private User user;
    private ImageButton subHiveButton;
    private ImageAdapter imageAdapter;
    private List<Hive> hives;
    private TextView listEmptyTv;
    private ProgressBar progressBar;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = this;
        hiveBusiness = new HiveBusinessImpl();
        user = new User();
        user.setId(1);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hive_overview);

        gridView = findViewById(R.id.gridView);
        listEmptyTv = findViewById(R.id.emptyListTV);
        progressBar = findViewById(R.id.progressBarOverview);

        setupSubscribedHives();

        setupToolbar();



        //recyclerView = findViewById(R.id.recyclerview);
        //layoutManager = new GridLayoutManager(this,2);
        //recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(layoutManager);

        //adapter = new RecyclerAdapter(hiveBusiness.getHives(user,2));
        //recyclerView.setAdapter(adapter);

        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);

        //RecyclerAdapter.ImageViewHolder();
        //Intent ID = new Intent(this, TestingHiveTool.class);
        //startActivity(ID);


    }

    void setupSubscribedHives() {

        new AsyncTask() {
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
                    hives = hiveBusiness.getHives(user, 1);
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
                    if (hives.size() == 0){
                        listEmptyTv.setVisibility(View.VISIBLE);
                    } else {
                        listEmptyTv.setVisibility(View.INVISIBLE);
                        imageAdapter = new ImageAdapter(ctx, hives);
                        gridView.setAdapter(imageAdapter);

                    }
                }
            }
        }.execute();
    }

    // Replaces action bar with toolbar
    public void setupToolbar() {
        // Sets the toolbar for the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // account logo button left side on toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_account_dark);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    // When back arrow button is pressed
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, PersonalSettings.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickHive(View view) {

/*
        Intent ID = new Intent(this, GraphActivity.class);
        String s = "" + recyclerView.getChildLayoutPosition(view);
        ID.putExtra("idString", s);

        ImageAdapter imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);
        */


    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        setupSubscribedHives();
    }

    @Override
    public void onClick(View view) {
        if (view == subHiveButton) {
            Intent ID = new Intent(this, SubscribeHiveActivityRecycl.class);
            startActivity(ID);
        }
    }
}
