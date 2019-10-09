package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import dk.dtu.group22.beeware.R;

public class HiveActivity extends AppCompatActivity {

    TextView hiveId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hive);

        Intent intent = getIntent();

        String i = intent.getStringExtra("label");

        hiveId = findViewById(R.id.HiveID);

        hiveId.setText(i);



    }


}
