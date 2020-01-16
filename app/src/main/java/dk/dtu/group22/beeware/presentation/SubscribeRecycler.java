package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

public class SubscribeRecycler extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Logic logic;
    private TextView status;
    private ProgressBar progressBar;
    private TextView activeTextbutton, inactiveTextbutton, subscriptionsTextbutton;
    private View underlineOne, underlineTwo, underlineThree;
    private List<NameIdPair> allHives;
    private List<NameIdPair> active = new ArrayList<>();
    private List<NameIdPair> inactive = new ArrayList<>();
    private List<NameIdPair> subscriptions = new ArrayList<>();
    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        // Initialisation
        logic = Logic.getSingleton();
        logic.setContext(this);

        status = findViewById(R.id.statusText);
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

        activeTextbutton = findViewById(R.id.subscribe_active_textbutton);
        activeTextbutton.setOnClickListener(this);
        subscriptionsTextbutton = findViewById(R.id.subscribe_subscriptions_textbutton);
        subscriptionsTextbutton.setOnClickListener(this);
        inactiveTextbutton = findViewById(R.id.subscribe_inactive_textbutton);
        inactiveTextbutton.setOnClickListener(this);
        underlineOne = findViewById(R.id.subscribe_underline1);
        underlineTwo = findViewById(R.id.subscribe_underline2);
        underlineThree = findViewById(R.id.subscribe_underline3);

        searchField = findViewById(R.id.subscribe_search_field);
        searchField.addTextChangedListener(textWatcher);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListElements(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadListElements(false);
    }

    // Gets the names and ids that is possible to subscribe to,
    // and adds them to the list via the 'SubscribeAdapter' class
    private void loadListElements(boolean run) {
        AsyncTask asyncTask = new AsyncTask() {
            String errorMsg = null;

            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                activeTextbutton.setEnabled(false);
                subscriptionsTextbutton.setEnabled(false);
                inactiveTextbutton.setEnabled(false);
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
                    status.setText(errorMsg);
                } else{
                    splitSubscriptions();
                }
            }
        };

        if (run) {
            asyncTask.execute();
        } else {
            asyncTask.cancel(true);
        }

    }

    // Splits all the hives into the ones that has been subscribed and the ones that has not
    private void splitSubscriptions() {
        List<Integer> subbedIds = logic.getSubscriptionIDs();

        boolean wasSubscribed;
        long twoDaysAgo = System.currentTimeMillis() - 172800000; // The big number is two days in millis

        // Checks each hive if it is in the list of subbeds ids
        for (int i = 0; i < allHives.size(); i++) {
            wasSubscribed = false;
            for (int id : subbedIds) {
                if (allHives.get(i).getID() == id) {
                    // The hive is in the list of subbed ids, so it adds it to
                    // the list of subscribed hives and removes it from the active
                    subscriptions.add(allHives.get(i));
                    wasSubscribed = true;
                    break;
                }
            }
            if (!wasSubscribed) {
                if (allHives.get(i).isActive()) {
                    active.add(allHives.get(i));
                } else {
                    inactive.add(allHives.get(i));
                }
            }
        }


        activeTextbutton.setEnabled(true);
        subscriptionsTextbutton.setEnabled(true);
        inactiveTextbutton.setEnabled(true);
        searchField.setEnabled(true);

        // Setting the list (recyclerview) to the active hives
        recyclerView.setAdapter(new SubscribeAdapter(active));
    }

    @Override
    public void onClick(View view) {
        if (view == activeTextbutton) {
            // If the user wants to see the active hives
            if (active.size() == 0) {
                status.setVisibility(View.VISIBLE);
                status.setText("List of active hives is empty.");
            } else {
                status.setVisibility(View.INVISIBLE);
                status.setText("");
            }
            searchField.setText("");
            underlineOne.setVisibility(View.VISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            underlineThree.setVisibility(View.INVISIBLE);
            recyclerView.setAdapter(new SubscribeAdapter(active));

        } else if (view == inactiveTextbutton) {
            // If the user wants to see the inactive hives
            if (active.size() == 0) {
                status.setVisibility(View.VISIBLE);
                status.setText("List of inactive hives is empty.");
            } else {
                status.setVisibility(View.INVISIBLE);
                status.setText("");
            }
            searchField.setText("");
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.VISIBLE);
            underlineThree.setVisibility(View.INVISIBLE);
            recyclerView.setAdapter(new SubscribeAdapter(inactive));

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.ProblematicHivesTitle))
                    .setMessage(getString(R.string.ProblematicHivesBody))
                    .setNegativeButton(getString(R.string.GoBack), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Takes the user back to the list of active hives
                            SubscribeRecycler.this.onClick(activeTextbutton);
                        }
                    })
                    .setPositiveButton(getString(R.string.Continue), null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#ff8624"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#ff8624"));

        } else if(view == subscriptionsTextbutton) {
            // If the user wants to see the hives the user has subscribed to
            if (subscriptions.size() == 0) {
                status.setVisibility(View.VISIBLE);
                status.setText("You have no subscriptions.");
            }
            searchField.setText("");
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            underlineThree.setVisibility(View.VISIBLE);
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
                underlineThree.setVisibility(View.INVISIBLE);
                recyclerView.setAdapter(new SubscribeAdapter(active));
            } else {
                // if the user has written something in the search field
                underlineOne.setVisibility(View.INVISIBLE);
                underlineTwo.setVisibility(View.INVISIBLE);
                underlineThree.setVisibility(View.INVISIBLE);
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
