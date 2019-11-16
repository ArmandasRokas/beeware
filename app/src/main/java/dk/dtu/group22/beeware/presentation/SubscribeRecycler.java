package dk.dtu.group22.beeware.presentation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.interfaces.NameIdPair;

public class SubscribeRecycler extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Logic logic;
    private TextView errorTv;
    private ProgressBar progressBar;
    private ImageView backArrow, moreMenu;
    private PopupMenu menuPopup;
    private boolean showingSubscriptions = false;
    List<NameIdPair> subscribable;
    List<NameIdPair> subscriptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        // Initialisation
        logic = Logic.getSingleton();
        logic.setContext(this);

        errorTv = findViewById(R.id.errorSubscribeHives);
        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.hivesToSubRV);

        //recyclerView.setHasFixedSize(true);
        // Linear layout manager for the recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        backArrow = findViewById(R.id.subscribe_back_arrow);
        backArrow.setOnClickListener(this);
        moreMenu = findViewById(R.id.subscribe_more_menu);
        moreMenu.setOnClickListener(this);

        // The vertical more menu is set to show the sub_mode_more_menu xml in menus
        menuPopup = new PopupMenu(this, moreMenu);
        menuPopup.getMenuInflater().inflate(R.menu.sub_mode_more_menu, menuPopup.getMenu());
        menuPopup.setOnMenuItemClickListener(this);

        addListElements();
    }

    // Gets the names and ids that is possible to subscribe to,
    // and adds them to the list via the 'SubscribeAdapter' class
    private void addListElements() {
        new AsyncTask() {
            List<NameIdPair> allHives;
            String errorMsg = null;

            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                moreMenu.setEnabled(false);
            }

            @Override
            protected Object doInBackground(Object... arg0) {
                try {
                    allHives = logic.getNamesAndIDs();
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
                    splitSubscriptions(allHives);
                }
            }
        }.execute();
    }

    // Splits all the hives into the ones that has been subscribed and the ones that has not
    private void splitSubscriptions(List<NameIdPair> allHives) {
        subscribable = allHives;
        ArrayList<Integer> subbedIds = logic.getSubscriptionIDs();

        for (int i = 0; i < subscribable.size(); i++) {
            for (int id : subbedIds) {
                if (subscribable.get(i).getID() == id) {
                    subscriptions.add(subscribable.get(i));
                    subscribable.remove(subscribable.get(i));
                }
            }
        }
        mAdapter = new SubscribeAdapter(subscribable);
        recyclerView.setAdapter(mAdapter);
        moreMenu.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view == backArrow) {
            finish();
        } else if (view == moreMenu) {
            menuPopup.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        // If user is viewing subscriptions else if user is viewing subscribable
        if (showingSubscriptions == false) {
            showingSubscriptions = true;
            menuItem.setTitle("Show non-subscribed only");
            if (subscriptions.size() == 0) {
                errorTv.setVisibility(View.VISIBLE);
                errorTv.setText("You have no subscriptions.");
            }
            mAdapter = new SubscribeAdapter(subscriptions);
            recyclerView.setAdapter(mAdapter);
        } else {
            showingSubscriptions = false;
            menuItem.setTitle("Show subscribed only");
            errorTv.setVisibility(View.INVISIBLE);
            errorTv.setText("");
            mAdapter = new SubscribeAdapter(subscribable);
            recyclerView.setAdapter(mAdapter);
        }
        return true;
    }
}

class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.MyViewHolder> {

    private ArrayList<Integer> subbedIds;
    private List<NameIdPair> mDataset;
    private Logic logic;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SubscribeAdapter(List<NameIdPair> myDataset) {
        mDataset = myDataset;
        logic = Logic.getSingleton();
        subbedIds = logic.getSubscriptionIDs();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public TextView subHiveName;
        public Switch subHiveSwitch;

        public MyViewHolder(View v) {
            super(v);
            subHiveName = v.findViewById(R.id.subscribe_name);
            subHiveSwitch = v.findViewById(R.id.subscribe_switch);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubscribeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_subscribe, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
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

        // Runs when the subscribe switch is being switched on/off
        holder.subHiveSwitch.setOnClickListener(
                v -> {
                    System.out.println("The switch is set to " + holder.subHiveSwitch.isChecked());
                    if (holder.subHiveSwitch.isChecked()) {
                        // Calls the DAL to save the id in preferenceManager
                        logic.subscribeHive(mDataset.get(position).getID());
                    } else {
                        // Deletes the hive id from the preferenceManager of saved subs in DAL
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
