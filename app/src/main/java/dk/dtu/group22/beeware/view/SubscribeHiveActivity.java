package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.entities.User;

public class SubscribeHiveActivity extends AppCompatActivity {
    final HiveBusiness hiveBusiness = new HiveBusinessImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_hive);
        final EditText idET = findViewById(R.id.hiveIdET);

        Button confirmBtn = findViewById(R.id.confirmHiveIdSubscribeBtn);
        confirmBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Fix Hardcoded user. Armandas
                        User user = new User();
                        user.setId(1);
                        Hive hive = new Hive();
                        hive.setId(Integer.valueOf(idET.getText().toString()));
                        hiveBusiness.subscribeHive(user, hive);
                    }
                }
        );
    }
}
