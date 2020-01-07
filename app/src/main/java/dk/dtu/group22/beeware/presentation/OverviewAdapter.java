package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class OverviewAdapter extends BaseAdapter {

    Context ctx;
    List<Hive> hives;

    OverviewAdapter(Context ctx, List<Hive> hives) {
        this.ctx = ctx;
        this.hives = hives;

    }

    @Override
    public int getCount() {
        //amount of hives
        return hives.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return R.drawable.beehive2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView = convertView;

        if (gridView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.custom_hive, null);
        }

        ImageView img = gridView.findViewById(R.id.album);
        TextView title = gridView.findViewById(R.id.album_title);
        TextView lastUpdated = gridView.findViewById(R.id.last_updated_tv);
        TextView weight = gridView.findViewById(R.id.hive_currWeightTV);
        TextView temp = gridView.findViewById(R.id.hive_currTempTV);
        TextView illum = gridView.findViewById(R.id.hive_currIllumTV);

        //Set colors depending on status
        ImageView weightIcon = gridView.findViewById(R.id.weightIcon);
        ImageView illumIcon = gridView.findViewById(R.id.illumIcon);
        ImageView tempIcon = gridView.findViewById(R.id.tempIcon);

        List<Hive.Status> statuses = Arrays.asList(hives.get(position).getWeightStatus(), hives.get(position).getIllumStatus(), hives.get(position).getTempStatus());
        List<ImageView> icons = Arrays.asList(weightIcon, illumIcon, tempIcon);

        for (int i = 0; i < icons.size(); ++i) {
            ImageView currentIcon = icons.get(i);
            Hive.Status currentStatus = statuses.get(i);
            if (currentStatus == Hive.Status.UNDEFINED) {
                currentIcon.setColorFilter(Color.GRAY);
            } else if (currentStatus == Hive.Status.OK) {
                currentIcon.setColorFilter(Color.GREEN);
            } else if (currentStatus == Hive.Status.WARNING) {
                currentIcon.setColorFilter(Color.YELLOW);
            } else if (currentStatus == Hive.Status.DANGER) {
                currentIcon.setColorFilter(Color.RED);
            }
        }

        // A listener on the whole grid-element (each full 'hive square')
        gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ID = new Intent(ctx, GraphActivity.class);
                Hive clickedHiveHive = hives.get(position);
                ID.putExtra("hiveid", clickedHiveHive.getId());
                ID.putExtra("hivename", clickedHiveHive.getName());
                ID.putExtra("currentweight", (float) clickedHiveHive.getCurrWeight());
                ID.putExtra("weightdelta", (float) clickedHiveHive.getWeightDelta());
                ctx.startActivity(ID);
            }
        });

        title.setText(hives.get(position).getName());
        lastUpdated.setText("Sidst opdateret \n" + hives.get(position).getLastUpdated());
        char sign = hives.get(position).getWeightDelta() < 0 ? '\0' : '+';
        String formattedWeightDisplay = "";
        if (Double.isNaN(hives.get(position).getWeightDelta())) {
            formattedWeightDisplay = String.format("%.1fkg", hives.get(position).getCurrWeight());
        } else {
            formattedWeightDisplay = String.format("%.1fkg %c%.2f", hives.get(position).getCurrWeight(), sign, hives.get(position).getWeightDelta());
        }

        weight.setText(formattedWeightDisplay);
        temp.setText(String.format("%.1f\u2103", hives.get(position).getCurrTemp()));
        illum.setText(String.format("%.0flx", hives.get(position).getCurrIlluminance()));

        return gridView;
    }
}
