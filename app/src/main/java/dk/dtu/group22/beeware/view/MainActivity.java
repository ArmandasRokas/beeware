package dk.dtu.group22.beeware.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CONTENT_VIEW_ID = 10101010;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private HiveBusiness hiveBusiness;
    private User user;
    private ImageButton subHiveButton;


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

        subHiveButton = findViewById(R.id.subHiveBtn);
        subHiveButton.setOnClickListener(this);

        //RecyclerAdapter.ImageViewHolder();

        // account logo button left side on action bar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_account_logo);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accountbutton, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.accountbutton) {
            Intent i = new Intent(this, PersonalSettings.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View view) {
        if (view == subHiveButton) {
            Intent ID = new Intent(this, SubscribeHiveActivityRecycl.class);
            startActivity(ID);
        }
    }
}
