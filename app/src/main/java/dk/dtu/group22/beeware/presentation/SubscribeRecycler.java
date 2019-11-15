package dk.dtu.group22.beeware.presentation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dto.interfaces.NameIdPair;

public class SubscribeRecycler extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Logic logic;
    private TextView errorTv;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        logic = Logic.getSingleton();
        logic.setContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        setupToolbar();

        errorTv = findViewById(R.id.errorSubscribeHives);
        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.hivesToSubRV);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
        List<HighScore> highScores = highScoreRepo.getHighScores();
        String[] highScoresStringArray = highScores.stream()
                .map(x -> String.format("%s %s", x.getUsername(), x.getScore()))
                .toArray(String[]::new);
        */


        new AsyncTask() {
            List<NameIdPair> hivesToSub;
            String errorMsg = null;

            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hivesToSub = logic.getNamesAndIDs();
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
                if (errorMsg != null) {
                    errorTv.setText(errorMsg);
                } else{
                    mAdapter = new SubscribeAdapter(hivesToSub);
                    recyclerView.setAdapter(mAdapter);
                }
            }
        }.execute();

    }

    // Replaces action bar with custom_toolbar and sets the title of the activity right
    public void setupToolbar() {
        // Sets the custom_toolbar for the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Calculate ActionBar's height
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        params.setMarginEnd(actionBarHeight + 25);
        toolbar_title.setLayoutParams(params);

        // account logo button left side on custom_toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar_title.setText("Subscriptions");
    }

    // When back arrow button is pressed
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.MyViewHolder> {
    private ArrayList<Integer> subbedIds;
    private List<NameIdPair> mDataset;
    private Logic logic = Logic.getSingleton();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        //public TextView textView;
        public TextView subHiveName;
        public Switch subHiveSwitch;

        public MyViewHolder(View v) {
            super(v);
            //textView = v.findViewById(R.id.hiveNameTv);
            subHiveName = v.findViewById(R.id.subscribe_name);
            // TODO: Når recycleren genbruger switch'ene, tickes de ikke tilbage på off
            subHiveSwitch = v.findViewById(R.id.subscribe_switch);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SubscribeAdapter(List<NameIdPair> myDataset) {
        mDataset = myDataset;
        subbedIds = logic.getSubscriptionIDs();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubscribeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_subscribe, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //System.out.println((mDataset.get(position).getName()));
        //holder.textView.setText(mDataset.get(position).getName());
        holder.subHiveName.setText(mDataset.get(position).getName());

        // Checks if the current list position element is in list of subbed ids
        for (int id : subbedIds) {
            if (id == mDataset.get(position).getID()) {
                holder.subHiveSwitch.setChecked(true);
                break;
            } else {
                holder.subHiveSwitch.setChecked(false);
            }
        }

        holder.subHiveSwitch.setOnClickListener(
                v -> {
                    // TODO hardcoded user
                    System.out.println("The switch is set to " + holder.subHiveSwitch.isChecked());
                    if (holder.subHiveSwitch.isChecked()) {
                        // Calls the DAL to save the id in a file
                        logic.subscribeHive(mDataset.get(position).getID());
                    } else {
                        // Deletes the hive id from the file of saved subs in DAL
                        logic.unsubscribeHive(mDataset.get(position).getID());
                    }
                    subbedIds = logic.getSubscriptionIDs();
                }
        );


    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
