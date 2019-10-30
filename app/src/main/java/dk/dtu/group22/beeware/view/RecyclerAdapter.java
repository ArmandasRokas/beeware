package dk.dtu.group22.beeware.view;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dk.dtu.group22.beeware.R;

import static androidx.core.content.ContextCompat.startActivity;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder> {


//?????????????????????????????????????????????????????????????
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(view);

        return imageViewHolder;
    }
//??????????????????????????????
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        holder.album.setImageResource(R.drawable.beehive2);
        holder.albumTitle.setText("Hive " + position);
        holder.ID = position;
    }

    @Override
    public int getItemCount() {
        return 12;
    }
    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView album;
        TextView albumTitle;
        int ID;


        public ImageViewHolder (@NonNull View itemView) {
            super(itemView);
            album = itemView.findViewById(R.id.album);
            albumTitle = itemView.findViewById(R.id.album_title);
        }
K
        @Override
        public void onClick(View v) {


        }
    }

}
