package dk.dtu.group22.beeware.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.User;

public class MainActivity extends AppCompatActivity {

    private static final int CONTENT_VIEW_ID = 10101010;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private HiveBusiness hiveBusiness;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hiveBusiness = new HiveBusinessImpl();
        user = new User();
        user.setId(1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(hiveBusiness.getHives(user,2));
        recyclerView.setAdapter(adapter);

        //RecyclerAdapter.ImageViewHolder();

        Intent ID = new Intent(this, SubscribeHiveActivityRecycl.class);
        startActivity(ID);



    }
        public boolean onCreateOptionsMenu (Menu menu){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return super.onCreateOptionsMenu(menu);
        }


    public void onClickHive(View view) {


        Intent ID = new Intent(this, GraphActivity.class);
        String s = "" + recyclerView.getChildLayoutPosition(view);
        ID.putExtra("idString", s);

        startActivity(ID);

    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        adapter = new RecyclerAdapter(hiveBusiness.getHives(user,2));
        recyclerView.setAdapter(adapter);
    }
}

