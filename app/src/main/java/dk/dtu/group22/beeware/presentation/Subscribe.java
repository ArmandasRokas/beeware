package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import dk.dtu.group22.beeware.dal.dto.Hive;

public class Subscribe extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Logic logic;
    private TextView status;
    private ProgressBar progressBar;
   // private TextView activeTextbutton, inactiveTextbutton, subscriptionsTextbutton;
    private TextView activeTextbutton, inactiveTextbutton, subscriptionsTextbutton;
    private View underlineOne, underlineTwo, underlineThree;
    private List<NameIdPair> allHives = new ArrayList<>();
    private List<NameIdPair> active = new ArrayList<>();
    private List<NameIdPair> inactive = new ArrayList<>();
    private List<NameIdPair> subscriptions = new ArrayList<>();
    private EditText searchField;
    private int prevTab = 0, tab; // tab 1 = active, 2 = inactive, 3 = subscriptions
    private SubscribeAdapter currentAdapter;
    private List<Integer> subsIds;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        // Initialisation
        logic = Logic.getSingleton();
        logic.setContext(this);
        ctx = this;

        // Finding xml resources and setting listeners
        status = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.hivesToSubRV);
        searchField = findViewById(R.id.subscribe_search_field);
        searchField.addTextChangedListener(textWatcher);
        searchField.setOnTouchListener((view, motionEvent) -> {
            if(allHives .isEmpty()){
                loadListElements(true);
            }
            return false;
        });
        activeTextbutton = findViewById(R.id.subscribe_active_textbutton);
        activeTextbutton.setOnClickListener(this);
//        activeTextbutton = findViewById(R.id.subscribe_active_textbutton);
//        activeTextbutton.setOnClickListener(this);
        subscriptionsTextbutton = findViewById(R.id.subscribe_subscriptions_textbutton);
        subscriptionsTextbutton.setOnClickListener(this);
//        subscriptionsTextbutton = findViewById(R.id.subscribe_subscriptions_textbutton);
//        subscriptionsTextbutton.setOnClickListener(this);
        inactiveTextbutton = findViewById(R.id.subscribe_inactive_textbutton);
        inactiveTextbutton.setOnClickListener(this);
//        inactiveTextbutton = findViewById(R.id.subscribe_inactive_textbutton);
//        inactiveTextbutton.setOnClickListener(this);
        underlineOne = findViewById(R.id.subscribe_underline1);
        underlineTwo = findViewById(R.id.subscribe_underline2);
        underlineThree = findViewById(R.id.subscribe_underline3);

        // If the user has subscribed some hive, so by default open "Subscriptions", otherwise "Active"
        subsIds = logic.getSubscriptionIDs();
        if(subsIds.isEmpty()){
//            activeTextbutton_NotEmpty.setVisibility(View.GONE);
//            inactiveTextbutton_NotEmpty.setVisibility(View.GONE);
//            subscriptionsTextbutton_NotEmpty.setVisibility(View.GONE);
//            activeTextbutton.setVisibility(View.VISIBLE);
//            inactiveTextbutton.setVisibility(View.VISIBLE);
//            subscriptionsTextbutton.setVisibility(View.VISIBLE);
            tab = 1;
        } else {
//            activeTextbutton.setVisibility(View.GONE);
//            inactiveTextbutton.setVisibility(View.GONE);
//            subscriptionsTextbutton.setVisibility(View.GONE);
//            activeTextbutton_NotEmpty.setVisibility(View.VISIBLE);
//            inactiveTextbutton_NotEmpty.setVisibility(View.VISIBLE);
//            subscriptionsTextbutton_NotEmpty.setVisibility(View.VISIBLE);
            loadOnlySubscribedList();
            tab = 3;
        }
        changeTab(tab);

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

    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
        loadListElements(true);
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        loadListElements(false);
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private int getSessionSubs() {
        if (currentAdapter != null) {
            return currentAdapter.getSessionSubs();
        }
        return 0;
    }

    /**
     * Gets all hive names and ids
     *
     * @param run A boolean of whether to run the code or cancel it
     */
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
                searchField.setHint(getResources().getString(R.string.loading));
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
                } else {
                    splitSubscriptions();
                 //   changeTab(tab);
                }
                // active
                if (tab == 1) {
                    if(active.size() == 0) {
                        status.setVisibility(View.VISIBLE);
                        status.setText(getString(R.string.EmptyList));

                    }else {
                            status.setVisibility(View.INVISIBLE);
                            currentAdapter = new SubscribeAdapter(active, ctx, getSessionSubs(), tab);
                            recyclerView.setAdapter(currentAdapter);
                    }
                }
                // inactive hives
                if (tab == 2) {
                    if (inactive.size() == 0) {
                        status.setVisibility(View.VISIBLE);
                        status.setText(getString(R.string.EmptyList));
                    } else {
                        status.setVisibility(View.INVISIBLE);
                        currentAdapter = new SubscribeAdapter(inactive, ctx, getSessionSubs(), tab);
                        recyclerView.setAdapter(currentAdapter);
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

    private void loadOnlySubscribedList(){

        for(Integer id: subsIds){
           // Hive h = logic.getCachedHive(id);
//            if(h != null){
//                subscriptions.add(new NameIdPair(h.getName(), id, true, ""));
//            }
            NameIdPair nameIdPair = logic.getCachedNameIdPair(id);
            if(!nameIdPair.getName().contentEquals("Unknown hive")){
                subscriptions.add(nameIdPair);
            }

        }
        // Work around for search function to work before allHives is fetched
      //  active = subscriptions; // No more need for this work around
    }

    /**
     * Splits the hive array into three: active, inactive and subscribed ones
     */
    private void splitSubscriptions() {
        List<Integer> subbedIds = logic.getSubscriptionIDs();

        active = new ArrayList<>();
        inactive = new ArrayList<>();
        subscriptions = new ArrayList<>();

        // Checks each hive if it is in the list of subbeds ids
        for (int i = 0; i < allHives.size(); i++) {
            if (subbedIds.contains(allHives.get(i).getID())) {
                // The hive is in the list of subbed ids, so it adds it to
                // the list of subscribed hives and removes it from the active
                subscriptions.add(allHives.get(i));
            }

            if (allHives.get(i).isActive()) {
                active.add(allHives.get(i));
            } else {
                inactive.add(allHives.get(i));
            }
        }

        activeTextbutton.setEnabled(true);
        subscriptionsTextbutton.setEnabled(true);
        inactiveTextbutton.setEnabled(true);
        searchField.setEnabled(true);
        searchField.setHint(getResources().getString(R.string.search));
    }

    private void changeTab(int tab) {
        this.tab = tab;
        status.setVisibility(View.INVISIBLE); // Clear all status from a previous tab
        if (tab == 1) {
            // User wants to see the active hives
            if(allHives.isEmpty()){
                // Clear the list before fetching
                currentAdapter = new SubscribeAdapter(new ArrayList<NameIdPair>(), ctx, getSessionSubs(), tab);
                recyclerView.setAdapter(currentAdapter);
                // Fetch Active/Inactive hives from HiveTool
                loadListElements(true);
            } else {
                currentAdapter = new SubscribeAdapter(active, ctx, getSessionSubs(), tab);
                recyclerView.setAdapter(currentAdapter);
            }
//            if (active.size() == 0) {
//                status.setVisibility(View.VISIBLE);
//                status.setText(getString(R.string.EmptyList));
//            } else {
//                status.setVisibility(View.INVISIBLE);
//            }
            searchField.setText("");
            underlineOne.setVisibility(View.VISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            underlineThree.setVisibility(View.INVISIBLE);
            prevTab = 0;
        } else if (tab == 2) {
            // User wants to see the inactive hives
//            if (inactive.size() == 0) {
//                status.setVisibility(View.VISIBLE);
//                status.setText(getString(R.string.EmptyList));
//            } else {
//                status.setVisibility(View.INVISIBLE);
//            }
            if(allHives.isEmpty()){
                // Clear the list before fetching
                currentAdapter = new SubscribeAdapter(new ArrayList<NameIdPair>(), ctx, getSessionSubs(), tab);
                recyclerView.setAdapter(currentAdapter);
                // Fetch Active/Inactive hives from HiveTool
                loadListElements(true);
            } else {
                currentAdapter = new SubscribeAdapter(inactive, ctx, getSessionSubs(), tab);
                recyclerView.setAdapter(currentAdapter);
            }
            searchField.setText("");
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.VISIBLE);
            underlineThree.setVisibility(View.INVISIBLE);
            currentAdapter = new SubscribeAdapter(inactive, this, getSessionSubs(), tab);
            recyclerView.setAdapter(currentAdapter);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.ProblematicHivesTitle))
                    .setMessage(getString(R.string.ProblematicHivesBody))
                    .setNegativeButton(getString(R.string.GoBack), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Takes the user back to the list of active hives
                            if (prevTab == 0) {
                                // Do nothing
                            } else if (prevTab != 2) {
                                changeTab(prevTab);
                                prevTab = 0;
                            }
                        }
                    })
                    .setPositiveButton(getString(R.string.Continue), null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#ff8624"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#ff8624"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
        } else if (tab == 3) {
            // User wants to view their subscriptions
            if (subscriptions.size() == 0) {
                status.setVisibility(View.VISIBLE);
                status.setText(getString(R.string.NoSubscriptions));
            } else {
                status.setVisibility(View.INVISIBLE);
            }
            searchField.setText("");
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            underlineThree.setVisibility(View.VISIBLE);
            currentAdapter = new SubscribeAdapter(subscriptions, this, getSessionSubs(), tab);
            recyclerView.setAdapter(currentAdapter);

            prevTab = 0;
        } else {
            // The user is searching
            underlineOne.setVisibility(View.INVISIBLE);
            underlineTwo.setVisibility(View.INVISIBLE);
            underlineThree.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        hideKeyboard();
        prevTab = tab;
        if (view == activeTextbutton) {
            changeTab(1);
        } else if (view == inactiveTextbutton) {
            changeTab(2);
        } else if (view == subscriptionsTextbutton) {
            splitSubscriptions(); // Indlæs igen, i fald man har tilføjet nogle fra de andre faner
            changeTab(3);
        }
    }

    /**
     * Searches through hives when something is being written in the search bar
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().equals("")) {
                // If the user has deleted the letters to search through hives for
                if (tab == 0) {
                    changeTab(prevTab);
                    prevTab = 0;
                }
            } else {
                // if the user has written something in the search field
                status.setVisibility(View.INVISIBLE);
                if (prevTab == 0) {
                    prevTab = tab;
                }
                changeTab(0);
                List<NameIdPair> searchResults = new ArrayList<>();
                for (int j = 0; j < active.size(); j++) {
                    if (active.get(j).getName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        searchResults.add(active.get(j));
                    }
                }
                for (int j = 0; j < inactive.size(); j++) {
                    if (inactive.get(j).getName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        searchResults.add(inactive.get(j));
                    }
                }
                currentAdapter = new SubscribeAdapter(searchResults, Subscribe.this, getSessionSubs(), 0);
                recyclerView.setAdapter(currentAdapter);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

}
