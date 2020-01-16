package dk.dtu.group22.beeware.presentation;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

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
        public ConstraintLayout background;
        public TextView subHiveName;
        public TextView subHiveLocation;
        public Switch subHiveSwitch;

        public MyViewHolder(View v) {
            super(v);
            background = v.findViewById(R.id.sub_element_bg);
            subHiveName = v.findViewById(R.id.subscribe_name);
            subHiveSwitch = v.findViewById(R.id.subscribe_switch);
            subHiveLocation = v.findViewById(R.id.subHiveLocation);
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

        /*
        //Alternates the background colors of the list elements
        if (position % 2 == 0) {
            holder.background.setBackgroundColor(Color.parseColor("#E6A487"));
        } else {
            holder.background.setBackgroundColor(Color.parseColor("#FFB596"));
        }
        */

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.subHiveName.setText(mDataset.get(position).getName());
        holder.subHiveLocation.setText(mDataset.get(position).getLocation());

        // Changes the text color of the hive name to red if it is inactive
        if (!mDataset.get(position).isActive()) {
            holder.subHiveName.setTextColor(Color.argb(255,175,0,0));
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
