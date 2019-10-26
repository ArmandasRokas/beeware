package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;

public class SubscribeHiveActivityRecycl extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private HiveBusiness hiveBusiness;
    private TextView errorTv;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hiveBusiness = new HiveBusinessImpl();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_hive_recycl);
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
            List<Hive> hives;
            String errorMsg = null;
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    hives = hiveBusiness.getHivesToSubscribe();
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
                    errorTv.setText(errorMsg);
                } else{
                    mAdapter = new SubscribeHivesAdapter(hives);
                    recyclerView.setAdapter(mAdapter);
                }
            }
        }.execute();


    }

}

class SubscribeHivesAdapter extends RecyclerView.Adapter<SubscribeHivesAdapter.MyViewHolder> {
    private List<Hive> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        //public TextView textView;
        public Button subHiveBtn;
        public MyViewHolder(View v) {
            super(v);
            //textView = v.findViewById(R.id.hiveNameTv);
            subHiveBtn = v.findViewById(R.id.hiveToSubBtn);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SubscribeHivesAdapter(List<Hive> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubscribeHivesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hive_subscribe, parent, false);
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
        holder.subHiveBtn.setText(mDataset.get(position).getName());
        holder.subHiveBtn.setOnClickListener(
                v -> {
                    // TODO hiveBusiness.subscribeHive
                    System.out.println(mDataset.get(position).getId());
                }
        );
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
