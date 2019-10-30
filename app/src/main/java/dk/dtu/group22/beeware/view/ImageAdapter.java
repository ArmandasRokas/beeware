package dk.dtu.group22.beeware.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import dk.dtu.group22.beeware.R;

public class ImageAdapter extends BaseAdapter {

    Context ctx;

    ImageAdapter(Context ctx){

    this.ctx = ctx;
    }

    @Override
    public int getCount() {
        //amount of hives
        return 12;
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


        ImageView img = (ImageView)gridView.findViewById(R.id.myImage);
        TextView title = gridView.findViewById(R.id.album_title);

        img.setImageResource(R.drawable.beehive2);

        title.setText("Stade" + position);


        return gridView;
    }
}
