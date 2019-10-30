package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import dk.dtu.group22.beeware.R;

public class MainActivity extends AppCompatActivity {

    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hive_overview);

        gridView = findViewById(R.id.gridView);

        ImageAdapter imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

    }
}

