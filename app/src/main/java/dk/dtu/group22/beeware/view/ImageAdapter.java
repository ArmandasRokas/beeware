package dk.dtu.group22.beeware.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public class ImageAdapter extends BaseAdapter {

    Context ctx;
    List<Hive> hives;
    HiveBusiness hiveBusiness;

    ImageAdapter(Context ctx, List<Hive> hives){
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

        if(gridView == null){
            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.custom_image_layout, null );
        }


        ImageView img = (ImageView) gridView.findViewById(R.id.album);
        TextView title = gridView.findViewById(R.id.album_title);

        img.setImageResource(R.drawable.beehive2);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ID = new Intent(ctx, GraphActivity.class);
                ID.putExtra("hiveid", hives.get(position).getId());
                ID.putExtra("hivename", hives.get(position).getName());
                ctx.startActivity(ID);
            }
        });
        title.setText(hives.get(position).getName());


        return gridView;
    }
}
