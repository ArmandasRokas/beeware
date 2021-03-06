package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.NameIdPair;

class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.MyViewHolder> {
    private List<Integer> subbedIds;
    private List<NameIdPair> mDataset;
    private Logic logic;
    private int sessionSubs, tabNumber;
    private Context context;
    private Toast toast;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SubscribeAdapter(List<NameIdPair> myDataset, Context context, int sessionSubs, int tabNumber) {
        mDataset = myDataset;
        logic = Logic.getSingleton();
        subbedIds = logic.getSubscriptionIDs();
        this.context = context;
        this.sessionSubs = sessionSubs;
        this.tabNumber = tabNumber;
    }

    public int getSessionSubs() {
        return sessionSubs;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public ConstraintLayout background;
        public TextView subHiveName;
        public TextView subHiveLocation;
        public Switch subHiveSwitch;
        public ImageView settingsBtn;

        public MyViewHolder(View v) {
            super(v);
            background = v.findViewById(R.id.sub_element_bg);
            subHiveName = v.findViewById(R.id.subscribe_name);
            subHiveSwitch = v.findViewById(R.id.subscribe_switch);
            subHiveLocation = v.findViewById(R.id.subHiveLocation);
            settingsBtn = v.findViewById(R.id.hive_settings_btn);
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

        View.OnClickListener switchOnClickListener = view -> {
            if (holder.subHiveSwitch.isChecked()) {
                if (tabNumber != 3) {
                    --sessionSubs;
                }
                logic.unsubscribeHive(mDataset.get(position).getID());
                holder.subHiveSwitch.setChecked(false);
            } else {
                if (sessionSubs < 10) {
                    if (tabNumber != 3) {
                        sessionSubs++;
                    }
                    logic.subscribeHive(mDataset.get(position).getID());
                    holder.subHiveSwitch.setChecked(true);
                } else {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(context, "Save subscriptions before adding more", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        holder.subHiveName.setOnClickListener(switchOnClickListener);
        holder.subHiveLocation.setOnClickListener(switchOnClickListener);
//        holder.subHiveSwitch.setOnClickListener(switchOnClickListener); // Does not work correctly. Work around was to make subHiveName an sbubHiveLocation wider.

        holder.settingsBtn.setOnClickListener(view -> {
           // System.out.println("Settings clicked");
            Bundle bundle = new Bundle();
           // bundle.putBoolean("isFromGraph", false);
            bundle.putInt("hiveID", mDataset.get(position).getID());
            ConfigurationFragment fragment = new ConfigurationFragment();
            fragment.setArguments(bundle);
            fragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "configurationDialog");
        });

        if(tabNumber == 1 || tabNumber == 2){
            holder.settingsBtn.setVisibility(View.GONE);
        }

        //Alternates the background colors of the list elements
        if (position % 2 == 0) {
            holder.background.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        } else {
            holder.background.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        }

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.subHiveName.setText(mDataset.get(position).getName());

        String location = mDataset.get(position).getLocation();
        if (location.startsWith(",")) {
            location = location.substring(1);
        }
        if (location.startsWith(" ")) {
            location = location.substring(1);
        }
        holder.subHiveLocation.setText(location);

        // Changes the text color of the hive name to red if it is inactive
        if (!mDataset.get(position).isActive()) {
            holder.subHiveName.setTextColor(Color.argb(255, 175, 0, 0));
        } else {
            holder.subHiveName.setTextColor(Color.BLACK);
        }

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
    //    holder.itemView.setOnClickListener(switchOnClickListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
