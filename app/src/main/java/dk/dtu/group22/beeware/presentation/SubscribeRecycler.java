package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SubscribeRecycler extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Logic logic;
    private TextView errorTv;
    private ProgressBar progressBar;
    private ImageView backArrow;
    private TextView availableTextbutton, subscriptionsTextbutton;
    private View underlineOne, underlineTwo;
    private List<NameIdPair> allHives;
    private List<NameIdPair> subscribable = new ArrayList<>();
    private List<NameIdPair> subscriptions = new ArrayList<>();
    private EditText searchField;

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

        // Removes the keyboard when the recyclerview is touched (scrolling is going on)
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        });

        // Own back arrow
        backArrow = findViewById(R.id.subscribe_back_arrow);
        backArrow.setOnClickListener(this);

        availableTextbutton = findViewById(R.id.subscribe_available_textbutton);
        availableTextbutton.setOnClickListener(this);
        subscriptionsTextbutton = findViewById(R.id.subscribe_subscriptions_textbutton);
        subscriptionsTextbutton.setOnClickListener(this);
        underlineOne = findViewById(R.id.subscribe_underline1);
        underlineTwo = findViewById(R.id.subscribe_underline2);

        searchField = findViewById(R.id.subscribe_search_field);
        searchField.addTextChangedListener(textWatcher);

        loadListElements();
    }

    // Gets the names and ids that is possible to subscribe to,
    // and adds them to the list via the 'SubscribeAdapter' class
    private void loadListElements() {
        new AsyncTask() {
            String errorMsg = null;

            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                availableTextbutton.setEnabled(false);
                subscriptionsTextbutton.setEnabled(false);
                searchField.setEnabled(false);
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
                    splitSubscriptions();
                }
            }
        }.execute();
    }

    // Splits all the hives into the ones that has been subscribed and the ones that has not
    private void splitSubscriptions() {
        List<Integer> subbedIds = logic.getSubscriptionIDs();

        // Checks each hive if it is in the list of subbeds ids
        for (int i = 0; i < allHives.size(); i++) {
            // Adding the hive to the subscribable list no matter what
            subscribable.add(allHives.get(i));
            for (int id : subbedIds) {
                if (allHives.get(i).getID() == id) {
                    // The hive is in the list of subbed ids, so it adds it to
                    // the list of subscribed hives and removes it from the subscribable
                    subscriptions.add(allHives.get(i));
                    subscribable.remove(subscribable.size() - 1);
                }
            }
        }
        availableTextbutton.setEnabled(true);
        subscriptionsTextbutton.setEnabled(true);
        searchField.setEnabled(true);

        // Setting the list (recyclerview) to the subscribable hives
        recyclerView.setAdapter(new SubscribeAdapter(subscribable));
    }

    @Override
    public void onClick(View view) {
        if (view == backArrow) {
            finish();
        } else if (view == availableTextbutton) {
            // If the user wants to see the available (subscribable) hives
            errorTv.setVisibility(View.INVISIBLE);
            errorTv.setText("");
            searchField.setText("");
            underlineOne.setVisibility(View.VISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            recyclerView.setAdapter(new SubscribeAdapter(subscribable));
        } else if(view == subscriptionsTextbutton) {
            // If the user wants to see the subscribed hives
            if (subscriptions.size() == 0) {
                errorTv.setVisibility(View.VISIBLE);
                errorTv.setText("You have no subscriptions.");
            }
            searchField.setText("");
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new SubscribeAdapter(subscriptions));
        }
    }

    // A textwather to see if anything is being written in the edittext that applies it
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().equals("")) {
                // If the user has deleted the letters to search through hives for
                underlineOne.setVisibility(View.VISIBLE);
                underlineTwo.setVisibility(View.INVISIBLE);
                recyclerView.setAdapter(new SubscribeAdapter(subscribable));
            } else {
                // if the user has written something in the search field
                underlineOne.setVisibility(View.INVISIBLE);
                underlineTwo.setVisibility(View.INVISIBLE);
                List<NameIdPair> searchResults = new ArrayList<>();
                for (int j = 0; j < allHives.size(); j++) {
                    if (allHives.get(j).getName().toLowerCase()
                            .startsWith(charSequence.toString().toLowerCase())) {
                        // The given hive started with the searched term, so add it to a list
                        searchResults.add(allHives.get(j));
                    }
                }
                recyclerView.setAdapter(new SubscribeAdapter(searchResults));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

}

class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.MyViewHolder> {

    private List<Integer> subbedIds;
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

        // If a whole item on the recyclerview is pressed, then toggle the subscription switch
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.subHiveSwitch.isChecked()) {
                    logic.unsubscribeHive(mDataset.get(position).getID());
                    holder.subHiveSwitch.setChecked(false);
                } else {
                    logic.subscribeHive(mDataset.get(position).getID());
                    holder.subHiveSwitch.setChecked(true);
                }
                subbedIds = logic.getSubscriptionIDs();
            }
        });

        // Runs when the subscribe switch is being switched on/off
        holder.subHiveSwitch.setOnClickListener(
                v -> {
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
